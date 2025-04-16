(ns ex.readme-md
  (:require
   [com.jolygon.poly-map.api-0 :as poly :refer [poly-map]]))

(def m1 (poly-map :a 1 :b 2))
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

(require
 '[com.jolygon.poly-map.api-0 :as poly :refer [assoc-impl empty-poly-map make-poly-map]]
 '[com.jolygon.poly-map.api-0.keys :as pm]
 '[com.jolygon.poly-map.api-0.trans.keys :as tpm])

;; The vars in these namespaces evaluate to the actual namespaced keywords:
pm/get_k ;=> :com.jolygon.poly-map.api-0.keys/get_k
tpm/assoc_k_v ;=> :com.jolygon.poly-map.api-0.trans.keys/assoc_k_v

(def validating-map
  (-> empty-poly-map
      (assoc-impl
       ::pm/valAt_k_nf
       (fn [_this m _impls _metadata k _nf]
         (let [val (get m k ::nf)] ; Check underlying map
           (if (= val ::nf)
             (do (println (str "Key " k " not found, returning default!"))
                 :my-default) ; Return custom default
             val)))
       ::pm/valAt_k
       (fn [this m impls metadata k] ; Delegate to above
         ((::pm/valAt_k_nf impls) this m impls metadata k ::nf))

       ::pm/assoc_k_v
       ;   v   v   v  Watch for the recursion...
       (fn assoc_k_v [_this m impls metadata k v]
         (if-not (and (keyword? k) (number? v)) ; Are k and v valid?
           (throw (ex-info "Invalid assoc" {:key k :value v}))
           (make-poly-map
            (assoc m k v)
            (assoc impls ::pm/assoc_k_v assoc_k_v)
                                     ;; ^ ^ ^ ^ ^ notice the recursive definition
            metadata))))))

(def m3 (assoc validating-map :a 100))

(get m3 :a) ;=> 100
(get m3 :b) ;=> :my-default
(get m3 :b :different) ;=> :my-default (override ignores passed nf)

(try (assoc m3 "c" 200) (catch Exception e (ex-data e)))

;; transients

(def m (poly-map :a 1))

;; Create a transient version
(def tm (transient m))

;; Perform transient mutations
(assoc! tm :b 2)
(assoc! tm :c 3)

;; Convert back to persistent
(def m-final (persistent! tm))
#_m-final ;=> {:a 1, :b 2, :c 3}

;; --- Overriding Transient Operations ---
(def logging-when-transient-map
  (-> empty-poly-map
      (assoc-impl ::tpm/assoc_k_v
                  (fn [_this t-m impls metadata k v] ;; Note 'this' arg
                    (println "[Transient] assoc! key:" k "val:" v)
                    (poly/make-transient-poly-map
                     (java.util.concurrent.atomic.AtomicBoolean. true)
                     (assoc! t-m k v) impls metadata)))))

(persistent!
 (-> (transient logging-when-transient-map)
     (assoc! :x 100)
     (assoc! :y 200)))
; Prints: [Transient] assoc! key: :x val: 100
; Prints: [Transient] assoc! key: :y val: 200
;=> {:x 100, :y 200}
