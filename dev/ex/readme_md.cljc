(ns ex.readme-md
  (:require
   [com.jolygon.wrap-map :as w :refer [wrap]]))

(def m1 (wrap :a 1 :b 2))
#_m1
;=> {:a 1, :b 2}

;; It behaves like a standard Clojure map:
(get m1 :a)        ;=> 1
(get m1 :c 404)    ;=> 404
(:b m1)            ;=> 2
(count m1)         ;=> 2
(assoc m1 :c 3)    ;=> {:a 1, :b 2, :c 3}
(dissoc m1 :a)     ;=> {:b 2}
(keys m1)          ;=> (:a :b)
(vals m1)          ;=> (1 2)

;; It's persistent:
(def m2 (assoc m1 :c 3))
m1 ;=> {:a 1, :b 2}
m2 ;=> {:a 1, :b 2, :c 3}

;; Transient support:
(persistent! (assoc! (transient m1) :d 4))
;=> {:a 1, :b 2, :d 4}

(def validating-map
  (-> {}
      (w/assoc
        :-lookup_k_nf (fn [_ m k _nf]
                        (let [v (get m k ::nf)] ; Check underlying map
                          (if (= v ::nf)
                            (do (println (str "Key " k " not found, returning default!"))
                                :my-default) ; Return custom default
                            v)))
        :-lookup_k (fn [{:as e :keys [-lookup_k_nf]} m k] ; Delegate to above
                     (-lookup_k_nf e m k ::nf))

        :-assoc_k_v (fn [{:as e :keys [<-]} m k v]
                      (if-not (and (keyword? k) (number? v))
                        (throw (ex-info "Invalid assoc" {:key k :value v}))
                        (<- e (assoc m k v)))))))

(def m3 (assoc validating-map :a 100))
(get m3 :a) ;=> 100
(assoc m3 :a :bob)
; Execution error (ExceptionInfo) at (<cljs repl>:1).
; Invalid assoc
(get m3 :b) ;=> :my-default
(get m3 :b :different) ;=> :my-default (override ignores passed nf)


(-> m3
    transient
    (assoc! :x 100)
    (assoc! :y 200)
    persistent!)
;=> {:a 100, :x 100, :y 200}

(-> m3
    (assoc :b 200)
    (assoc "c" 200)
    (try (catch :default e (ex-data e))))
;=> {:key "c", :value 200}

;; transients

(def m (wrap :a 1))

;; Create a transient version
(def tm (transient m))

;; Perform transient mutations
(assoc! tm :b 2)
(assoc! tm :c 3)

;; Convert back to persistent
(def m-final (persistent! tm))
#_m-final ;=> {:c 3, :b 2, :a 1}

;; --- Overriding Transient Operations ---
(def logging-when-transient-map
  (-> {}
      (w/assoc
        :T_-assoc!_k_v (fn [_ t-m k v]
                         (println "[Transient] assoc! key:" k "val:" v)
                         (assoc! t-m k v)))))

(persistent!
 (-> (transient logging-when-transient-map)
     (assoc! :x 100)
     (assoc! :y 200)))
; Prints: [Transient] assoc! key: :x val: 100
; Prints: [Transient] assoc! key: :y val: 200
;=> {:x 100, :y 200}

(-> {:a 1}
    (w/assoc :T_-assoc!_k_v
             (fn [_ t-m k v]
               (println "[Transient] assoc! key:" k "val:" v)
               (assoc! t-m k v)))
    transient
    (assoc! :x 100)
    (assoc! :y 200)
    persistent!
    (assoc :b 2)
    w/unwrap ;; <- subsequent `assoc!` calls will not be logged
    transient
    (assoc! :r 4)
    (assoc! :s 5)
    persistent!
    (assoc :c 3))
; [Transient] assoc! key: :x val: 100
; [Transient] assoc! key: :y val: 200
;=> {:a 1, :x 100, :y 200, :b 2, :r 4, :s 5, :c 3}
