(ns ex.clj-bench
  (:require
   [libra.bench :refer [defbench is dur]]
   [libra.criterium :as c]
   [com.jolygon.wrap-map :as w :refer [wrap empty-wrap]]))

(do

  (def small-std-map {:a 1 :b 2 :c 3})
  (def small-wrap-map (wrap :a 1 :b 2 :c 3))
  (def large-map-size 10000)
  (def large-std-map (into {} (mapv (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))
  (def large-wrap-map (wrap large-std-map))
  (def keys-to-access (vec (keys large-std-map)))
  (defn rand-key [] (rand-nth keys-to-access))

  ;; Example Overrides
  (def log-atom (atom 0))
  (defn logging-assoc-impl [{:as e :keys [<-]} m k v]
    (swap! log-atom inc)
    (<- e (assoc m k v)))

  (defn validating-assoc-impl [{:as e :keys [<-]} m k v]
    (if (keyword? k)
      (<- e (assoc m k v))
      (throw (ex-info "Invalid key" {:key k}))))

  (def logged-wrap-map (w/assoc small-wrap-map :assoc_k_v logging-assoc-impl))
  (def validated-wrap-map (w/assoc large-wrap-map :assoc_k_v validating-assoc-impl))

  :end)
;; #_#_#_#_
(defbench baseline-read-large-standard-map
  (println :###########___Bench-1___###########)
  (is (dur 10000 (get large-std-map (rand-key)))))
;; #_
(defbench baseline-read-large-standard-map-criterium
  (is (c/bench (get large-std-map (rand-key)))))

(defbench baseline-read-large-wrap-map
  (is (dur 10000 (get large-wrap-map (rand-key)))))
;; #_
(defbench baseline-read-large-wrap-map-criterium
  (is (c/bench (get large-wrap-map (rand-key))))
  (println "\n\n"))


;; #_#_#_#_
(defbench baseline-read-missing-key-standard
  (println :###########___Bench-2___###########)
  (is (dur 10000 (get large-std-map :not-a-key :default-val))))
;; #_
(defbench baseline-read-missing-key-standard-criterium
  (is (c/bench (get large-std-map :not-a-key :default-val))))

(defbench baseline-read-missing-key-wrap
  (is (dur 10000 (get large-wrap-map :not-a-key :default-val))))
;; #_
(defbench baseline-read-missing-key-wrap-criterium
  (is (c/bench (get large-wrap-map :not-a-key :default-val)))
  (println "\n\n"))


;; #_#_#_#_
(defbench baseline-write-large-map-update-standard
  (println :###########___Bench-3___###########)
  (is (dur 10000 (assoc large-std-map (rand-key) 999))))
;; #_
(defbench baseline-write-large-map-update-standard-criterium
  (is (c/bench (assoc large-std-map (rand-key) 999))))

(defbench baseline-write-large-map-update-wrap
  (is (dur 10000 (assoc large-wrap-map (rand-key) 999))))
;; #_
(defbench baseline-write-large-map-update-wrap-criterium
  (is (c/bench (assoc large-wrap-map (rand-key) 999)))
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

(defbench baseline-reduce-large-map-sum-values-wrap
  (is (dur 10000 (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-wrap-map))))
;; #_
(defbench baseline-reduce-large-map-sum-values-wrap-criterium
  (is (c/bench (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-wrap-map)))
  (println "\n\n"))


(def large-map-data (vec (mapcat (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))

;; #_#_#_#_
(defbench baseline-construct-large-map-into-standard
  (println :###########___Bench-5___###########)
  (is (dur 1000 (into {} (mapv vec (partition 2 large-map-data))))))
;; #_
(defbench baseline-construct-large-map-into-standard-criterium
  (is (c/bench (into {} (mapv vec (partition 2 large-map-data))))))

(defbench baseline-construct-large-map-into-wrap
  (is (dur 1000 (into empty-wrap (mapv vec (partition 2 large-map-data))))))
;; #_
(defbench baseline-construct-large-map-into-wrap-criterium
  (is (c/bench (into empty-wrap (mapv vec (partition 2 large-map-data)))))
  (println "\n\n"))


;; #_#_#_#_
(defbench baseline-construct-large-map-apply-standard
  (println :###########___Bench-6___###########)
  (is (dur 1000 (apply hash-map large-map-data))))
;; #_
(defbench baseline-construct-large-map-apply-standard-criterium
  (is (c/bench (apply hash-map large-map-data))))

(defbench baseline-construct-large-map-apply-wrap
  (is (dur 1000 (apply wrap large-map-data))))
;; #_
(defbench baseline-construct-large-map-apply-wrap-criterium
  (is (c/bench (apply wrap large-map-data)))
  (println "\n\n"))


;; #_#_#_#_#_#_
(defbench override-impact-simple-assoc-standard
  (println :###########___Bench-7___###########)
  (is (dur 10000 (assoc small-std-map :d 4))))
;; #_
(defbench override-impact-simple-assoc-standard-criterium
  (is (c/bench (assoc small-std-map :d 4))))

(defbench override-impact-simple-assoc-wrap
  (is (dur 10000 (assoc small-wrap-map :d 4))))
;; #_
(defbench override-impact-simple-assoc-wrap-criterium
  (is (c/bench (assoc small-wrap-map :d 4))))

(defbench override-impact-simple-logging-assoc-wrap
  (is (dur 10000 (assoc logged-wrap-map :d 4))))
;; #_
(defbench override-impact-simple-logging-assoc-wrap-criterium
  (is (c/bench (assoc logged-wrap-map :d 4)))
  (println "\n\n"))


;; #_#_#_#_#_#_
(defbench override-impact-large-assoc-new-key-standard
  (println :###########___Bench-8___###########)
  (is (dur 1000 (assoc large-std-map :d 4))))
;; #_
(defbench override-impact-large-assoc-new-key-standard-criterium
  (is (c/bench (assoc large-std-map :d 4))))

(defbench override-impact-large-assoc-new-key-wrap
  (is (dur 1000 (assoc large-wrap-map :d 4))))
;; #_
(defbench override-impact-large-assoc-new-key-wrap-criterium
  (is (c/bench (assoc large-wrap-map :d 4))))

(defbench override-impact-large-validated-assoc-new-key-wrap
  (is (dur 1000 (assoc validated-wrap-map :d 4))))
;; #_
(defbench override-impact-large-validated-assoc-new-key-wrap-criterium
  (is (c/bench (assoc validated-wrap-map :d 4)))
  (println "\n\n"))


(def items-to-add (vec (range large-map-size)))

;; #_#_#_#_
(defbench transient-batch-assoc!-standard
  (println :###########___Bench-9___###########)
  (is (dur 1000 (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient {}) items-to-add)))))
;; #_
(defbench transient-batch-assoc!-standard-criterium
  (is (c/bench (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient {}) items-to-add)))))

(defbench transient-batch-assoc!-wrap
  (is (dur 1000 (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient empty-wrap) items-to-add)))))
;; #_
(defbench transient-batch-assoc!-wrap-criterium
  (is (c/bench (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient empty-wrap) items-to-add)))))

(def logged-transient-map
  (-> empty-wrap
      (w/assoc
        :T_assoc_k_v
        (fn [_ t-m k v]
          (swap! log-atom inc)
          (assoc! t-m k v)))))

;; #_#_
(defbench transient-batch-assoc!-logging-wrap
  (is (dur 1000 (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient logged-transient-map) items-to-add)))))
;; #_
(defbench transient-batch-assoc!-logging-wrap-criterium
  (is (c/bench (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient logged-transient-map) items-to-add))))
  (println "\n\n"))


;; #_#_#_#_
(defbench transient-persistent!-cost-standard
  (println :###########___Bench-10___###########)
  (is (dur 1000 (persistent! (transient large-std-map)))))
;; #_
(defbench transient-persistent!-cost-standard-criterium
  (is (c/bench (persistent! (transient large-std-map)))))

(defbench transient-persistent!-cost-wrap
  (is (dur 1000 (persistent! (transient large-wrap-map)))))
;; #_
(defbench transient-persistent!-cost-wrap-criterium
  (is (c/bench (persistent! (transient large-wrap-map))))
  (println "\n\n"))


(def counter (atom 0))
(def contended-wrap-map
  (-> empty-wrap
      (w/assoc
        :T_assoc_k_v
        (fn [_ t-m k v]
          (swap! counter inc)
          (assoc! t-m k v)))))

(defn contended-wrap-update [n-updates]
  (reset! counter 0)
  (let [futures (doall (for [_ (range 10)] ; Simulate 10 threads
                         (future
                           (persistent!
                            (reduce (fn [t i] (assoc! t (keyword (str "k" i)) i))
                                    (transient contended-wrap-map)
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

(defbench transient-contented-wrap
  (is (dur 10000 (contended-wrap-update 100))))
;; #_
(defbench transient-contented-wrap-criterium
  (is (c/bench (contended-wrap-update 100)))
  (println "\n\n"))
