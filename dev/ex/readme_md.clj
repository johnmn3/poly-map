(ns ex.readme-md
  (:require
   [com.jolygon.wrap-map.api-0 :as w :refer [wrap]]))

(def m1 (wrap :a 1 :b 2))
#_m1
;=> {:a 1, :b 2}

;; It behaves like a standard Clojure map:
(get m1 :a)        ;=> 1
(get m1 :c 404)    ;=> 404
(:b m1)            ;=> 2
(count m1)         ;=> 2
(assoc m1 :c 3)    ;=> {:c 3, :b 2, :a 1}
(dissoc m1 :a)     ;=> {:b 2}
(keys m1)          ;=> (:a :b)
(vals m1)          ;=> (1 2)

;; It's persistent:
(def m2 (assoc m1 :c 3))
m1 ;=> {:b 2, :a 1}
m2 ;=> {:c 3, :b 2, :a 1}

;; Transient support:
(persistent! (assoc! (transient m1) :d 4))
;=> {:b 2, :d 4, :a 1}

(def validating-map
  (-> {}
      (w/assoc
        :valAt_k_nf (fn [_ m k _nf]
                      (let [v (get m k ::nf)] ; Check underlying map
                        (if (= v ::nf)
                          (do (println (str "Key " k " not found, returning default!"))
                              :my-default) ; Return custom default
                          v)))
        :valAt_k
        (fn [{:as e :keys [valAt_k_nf]} m k] ; Delegate to above
          (valAt_k_nf e m k ::nf))
        :assoc_k_v
        (fn [{:as e :keys [<-]} m k v]
          (if-not (and (keyword? k) (number? v)) ; Are k and v valid?
            (throw (ex-info "Invalid assoc" {:key k :value v}))
            (<- e (assoc m k v)))))))

(def m3 (assoc validating-map :a 100))
(get m3 :a) ;=> 100
(get m3 :b) ;=> :my-default
(get m3 :b :different) ;=> :my-default (override ignores passed nf)

(try (assoc m3 "c" 200) (catch Exception e (ex-data e)))

;; transients

(def m1 (wrap :a 1))

;; Create a transient version
(def tm (transient m1))

1;; Perform transient mutations
(assoc! tm :b 2)
(assoc! tm :c 3)

;; Convert back to persistent
(def m-final (persistent! tm))
#_m-final ;=> {:c 3, :b 2, :a 1}

;; --- Overriding Transient Operations ---
(def logging-when-transient-map
  (-> {}
      (w/assoc
        :T_assoc_k_v (fn [_ t-m k v]
                       (println "[Transient] assoc! key:" k "val:" v)
                       (assoc! t-m k v)))))

(persistent!
 (-> (transient logging-when-transient-map)
     (assoc! :x 100)
     (assoc! :y 200)))
; Prints: [Transient] assoc! key: :x val: 100
; Prints: [Transient] assoc! key: :y val: 200
;=> {:x 100, :y 200}


;; --- Overriding Transient Operations ---
(def logging-when-transient-map-on-plain-map
  (-> {}
      (w/assoc
        :T_assoc_k_v (fn [_ t-m k v]
                       (println "[Transient] assoc! key:" k "val:" v)
                       (assoc! t-m k v)))))

(persistent!
 (-> (transient logging-when-transient-map-on-plain-map)
     (assoc! :x 100)
     (assoc! :y 200)))
; Prints: [Transient] assoc! key: :x val: 100
; Prints: [Transient] assoc! key: :y val: 200
;=> {:x 100, :y 200}

;; Now all in a row!

(-> {:a 1}
    (assoc :b 2)
    (w/assoc
      :T_assoc_k_v (fn [_ t-m k v]
                     (println "[Transient] assoc! key:" k "val:" v)
                     (assoc! t-m k v)))
    transient
    (assoc! :x 100)
    (assoc! :y 200)
    persistent!
    w/unwrap
    (dissoc :b)
    (w/assoc
      :assoc_k_v (fn [{:as e :keys [<-]} m k v]
                   (println "[Persistent] assoc key:" k "val:" v)
                   (<- e (assoc m k v))))
    (assoc :z 300)
    w/unwrap
    (assoc :done 1))
; [Transient] assoc! key: :x val: 100
; [Transient] assoc! key: :y val: 200
; [Persistent] assoc key: :z val: 300
{:a 1, :x 100, :y 200, :z 300, :done 1}

;; `{:a 1}` became side-effecting halfway through a pipeline, then back to a normal map, continuing through the pipeline.


;; Just do it with w/assoc:

(-> {:a 1}
    (assoc :b 2)
    (w/assoc :T_assoc_k_v (fn [_ t-m k v]
                            (println "[Transient] assoc! key:" k "val:" v)
                            (assoc! t-m k v)))
    transient
    (assoc! :x 100)
    (assoc! :y 200)
    persistent!
    w/unwrap
    (dissoc :b)
    (assoc :z 300))
; [Transient] assoc! key: :x val: 100
; [Transient] assoc! key: :y val: 200
;=> {:a 1, :x 100, :y 200, :z 300}
