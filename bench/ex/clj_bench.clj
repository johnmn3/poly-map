(ns ex.clj-bench
  (:require
   [libra.bench :refer [defbench is dur]]
   [libra.criterium :as c]
   [com.jolygon.poly-map.api-0 :as poly :refer [poly-map empty-poly-map]]
   [com.jolygon.poly-map.api-0.keys :as pm]
   [com.jolygon.poly-map.api-0.trans.keys :as tpm]))

(do

  (def small-std-map {:a 1 :b 2 :c 3})
  (def small-poly-map (poly-map :a 1 :b 2 :c 3))
  (def large-map-size 10000)
  (def large-std-map (into {} (mapv (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))
  (def large-poly-map (poly-map large-std-map))
  (def keys-to-access (vec (keys large-std-map)))
  (defn rand-key [] (rand-nth keys-to-access))

  ;; Example Overrides
  (def log-atom (atom 0))
  (defn logging-assoc-impl [_this m impls metadata k v]
    (swap! log-atom inc)
    (poly/make-poly-map (assoc m k v) impls metadata))

  (defn validating-assoc-impl [_this m impls metadata k v]
    (if (keyword? k)
      (poly/make-poly-map (assoc m k v) impls metadata)
      (throw (ex-info "Invalid key" {:key k}))))

  (def logged-poly-map (poly/assoc-impl small-poly-map ::pm/assoc_k_v logging-assoc-impl))
  (def validated-poly-map (poly/assoc-impl large-poly-map ::pm/assoc_k_v validating-assoc-impl))

  (def invoke-override-map
    (poly/assoc-impl (poly-map :factor 2)
                     ::pm/invoke-variadic
                     (fn [_this m _impls _metadata x] (* (:factor m) x))))

  :end)
;; #_#_#_#_
(defbench baseline-read-large-standard-map
  (println :###########___Bench-1___###########)
  (is (dur 10000 (get large-std-map (rand-key)))))
;; #_
(defbench baseline-read-large-standard-map-criterium
  (is (c/bench (get large-std-map (rand-key)))))

(defbench baseline-read-large-poly-map
  (is (dur 10000 (get large-poly-map (rand-key)))))
;; #_
(defbench baseline-read-large-poly-map-criterium
  (is (c/bench (get large-poly-map (rand-key))))
  (println "\n\n"))


;; #_#_#_#_
(defbench baseline-read-missing-key-standard
  (println :###########___Bench-2___###########)
  (is (dur 10000 (get large-std-map :not-a-key :default-val))))
;; #_
(defbench baseline-read-missing-key-standard-criterium
  (is (c/bench (get large-std-map :not-a-key :default-val))))

(defbench baseline-read-missing-key-poly
  (is (dur 10000 (get large-poly-map :not-a-key :default-val))))
;; #_
(defbench baseline-read-missing-key-poly-criterium
  (is (c/bench (get large-poly-map :not-a-key :default-val)))
  (println "\n\n"))


;; #_#_#_#_
(defbench baseline-write-large-map-update-standard
  (println :###########___Bench-3___###########)
  (is (dur 10000 (assoc large-std-map (rand-key) 999))))
;; #_
(defbench baseline-write-large-map-update-standard-criterium
  (is (c/bench (assoc large-std-map (rand-key) 999))))

(defbench baseline-write-large-map-update-poly
  (is (dur 10000 (assoc large-poly-map (rand-key) 999))))
;; #_
(defbench baseline-write-large-map-update-poly-criterium
  (is (c/bench (assoc large-poly-map (rand-key) 999)))
  (println "\n\n"))

#_
(let [r1 (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map)
      r2 (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map)]
  (println :same-reducing? (= r1 r2))
  (println :r1 r1)
  (println :r2 r2))

;; #_#_#_#_
(defbench baseline-reduce-large-map-sum-values-standard
  (println :###########___Bench-4___###########)
  (is (dur 10000 (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map))))
;; #_
(defbench baseline-reduce-large-map-sum-values-standard-criterium
  (is (c/bench (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map))))

(defbench baseline-reduce-large-map-sum-values-poly
  (is (dur 10000 (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-poly-map))))
;; #_
(defbench baseline-reduce-large-map-sum-values-poly-criterium
  (is (c/bench (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-poly-map)))
  (println "\n\n"))


(def large-map-data (vec (mapcat (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))

;; #_#_#_#_
(defbench baseline-construct-large-map-into-standard
  (println :###########___Bench-5___###########)
  (is (dur 1000 (into {} (mapv vec (partition 2 large-map-data))))))
;; #_
(defbench baseline-construct-large-map-into-standard-criterium
  (is (c/bench (into {} (mapv vec (partition 2 large-map-data))))))

(defbench baseline-construct-large-map-into-poly
  (is (dur 1000 (into empty-poly-map (mapv vec (partition 2 large-map-data))))))
;; #_
(defbench baseline-construct-large-map-into-poly-criterium
  (is (c/bench (into empty-poly-map (mapv vec (partition 2 large-map-data)))))
  (println "\n\n"))


;; #_#_#_#_
(defbench baseline-construct-large-map-apply-standard
  (println :###########___Bench-6___###########)
  (is (dur 1000 (apply hash-map large-map-data))))
;; #_
(defbench baseline-construct-large-map-apply-standard-criterium
  (is (c/bench (apply hash-map large-map-data))))

(defbench baseline-construct-large-map-apply-poly
  (is (dur 1000 (apply poly-map large-map-data))))
;; #_
(defbench baseline-construct-large-map-apply-poly-criterium
  (is (c/bench (apply poly-map large-map-data)))
  (println "\n\n"))


;; #_#_#_#_#_#_
(defbench override-impact-simple-assoc-standard
  (println :###########___Bench-7___###########)
  (is (dur 10000 (assoc small-std-map :d 4))))
;; #_
(defbench override-impact-simple-assoc-standard-criterium
  (is (c/bench (assoc small-std-map :d 4))))

(defbench override-impact-simple-assoc-poly
  (is (dur 10000 (assoc small-poly-map :d 4))))
;; #_
(defbench override-impact-simple-assoc-poly-criterium
  (is (c/bench (assoc small-poly-map :d 4))))

(defbench override-impact-simple-logging-assoc-poly
  (is (dur 10000 (assoc logged-poly-map :d 4))))
;; #_
(defbench override-impact-simple-logging-assoc-poly-criterium
  (is (c/bench (assoc logged-poly-map :d 4)))
  (println "\n\n"))


;; #_#_#_#_#_#_
(defbench override-impact-large-assoc-new-key-standard
  (println :###########___Bench-8___###########)
  (is (dur 1000 (assoc large-std-map :d 4))))
;; #_
(defbench override-impact-large-assoc-new-key-standard-criterium
  (is (c/bench (assoc large-std-map :d 4))))

(defbench override-impact-large-assoc-new-key-poly
  (is (dur 1000 (assoc large-poly-map :d 4))))
;; #_
(defbench override-impact-large-assoc-new-key-poly-criterium
  (is (c/bench (assoc large-poly-map :d 4))))

(defbench override-impact-large-validated-assoc-new-key-poly
  (is (dur 1000 (assoc validated-poly-map :d 4))))
;; #_
(defbench override-impact-large-validated-assoc-new-key-poly-criterium
  (is (c/bench (assoc validated-poly-map :d 4)))
  (println "\n\n"))


(def items-to-add (vec (range large-map-size)))

;; #_#_#_#_
(defbench transient-batch-assoc!-standard
  (println :###########___Bench-9___###########)
  (is (dur 1000 (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient {}) items-to-add)))))
;; #_
(defbench transient-batch-assoc!-standard-criterium
  (is (c/bench (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient {}) items-to-add)))))

(defbench transient-batch-assoc!-poly
  (is (dur 1000 (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient empty-poly-map) items-to-add)))))
;; #_
(defbench transient-batch-assoc!-poly-criterium
  (is (c/bench (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient empty-poly-map) items-to-add)))))

(def logged-transient-map
  (-> empty-poly-map
      (poly/assoc-impl
        ::tpm/assoc_k_v
        (fn [_this t-m impls metadata k v]
          (swap! log-atom inc)
          (poly/make-transient-poly-map (java.util.concurrent.atomic.AtomicBoolean. true) (assoc! t-m k v) impls metadata)))))

;; #_#_
(defbench transient-batch-assoc!-logging-poly
  (is (dur 1000 (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient logged-transient-map) items-to-add)))))
;; #_
(defbench transient-batch-assoc!-logging-poly-criterium
  (is (c/bench (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient logged-transient-map) items-to-add))))
  (println "\n\n"))


;; #_#_#_#_
(defbench transient-persistent!-cost-standard
  (println :###########___Bench-10___###########)
  (is (dur 1000 (persistent! (transient large-std-map)))))
;; #_
(defbench transient-persistent!-cost-standard-criterium
  (is (c/bench (persistent! (transient large-std-map)))))

(defbench transient-persistent!-cost-poly
  (is (dur 1000 (persistent! (transient large-poly-map)))))
;; #_
(defbench transient-persistent!-cost-poly-criterium
  (is (c/bench (persistent! (transient large-poly-map))))
  (println "\n\n"))


(def counter (atom 0))
(def contended-poly-map
  (poly/assoc-impl empty-poly-map
                   ::tpm/assoc_k_v
                   (fn [_this t-m impls metadata k v]
                     (swap! counter inc)
                     (poly/make-transient-poly-map (java.util.concurrent.atomic.AtomicBoolean. true) (assoc! t-m k v) impls metadata))))

(defn contended-poly-update [n-updates]
  (reset! counter 0)
  (let [futures (doall (for [_ (range 10)] ; Simulate 10 threads
                         (future
                           (persistent!
                            (reduce (fn [t i] (assoc! t (keyword (str "k" i)) i))
                                    (transient contended-poly-map)
                                    (range n-updates))))))]
    (run! deref futures))) ; Wait for all futures

(def contended-std-map {})

(defn contended-std-update [n-updates]
  (let [futures (doall (for [_ (range 10)] ; Simulate 10 threads
                         (future
                           (persistent!
                            (reduce (fn [t i] (assoc! t (keyword (str "k" i)) i))
                                    (transient contended-std-map)
                                    (range n-updates))))))]
    (run! deref futures))) ; Wait for all futures

(defbench transient-contended-standard
  (println :###########___Bench-11___###########)
  (is (dur 10000 (contended-std-update 100))))
;; #_
(defbench transient-contended-standard-criterium
  (is (c/bench (contended-std-update 100))))

(defbench transient-contented-poly
  (is (dur 10000 (contended-poly-update 100))))
;; #_
(defbench transient-contented-poly-criterium
  (is (c/bench (contended-poly-update 100)))
  (println "\n\n"))
