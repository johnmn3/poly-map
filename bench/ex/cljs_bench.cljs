(ns ex.cljs-bench
  (:require
    [com.jolygon.poly-map.api-0 :as poly :refer [poly-map]]
    [com.jolygon.poly-map.api-0.keys :as pm]))
    ;; [com.jolygon.poly-map.api-0.trans.keys :as tpm]))

(def small-std-map {:a 1 :b 2 :c 3})

(def small-poly-map (poly-map :a 1 :b 2 :c 3))

(def large-map-size 10000)

(def large-std-map (into {} (mapv (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))

(def large-poly-map (doall (into poly/empty-poly-map large-std-map)))

(def keys-to-access (vec (keys large-std-map)))

(defn rand-key [] (rand-nth keys-to-access))

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

(def large-map-data (vec (mapcat (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))

(def items-to-add (vec (range large-map-size)))

(defn ascii-bar-chart
  "Generates a 25-char wide ASCII bar chart comparing a percentage to 100%
   in the specified format. Percentage should be provided as a number (e.g., 91.3)."
  [percentage]
  (let [width 25
        scale 4.0
        num-chars (-> (/ (double percentage) scale)
                      (Math/round)
                      (long)
                      (max 0))
        poly-bar (str "|"
                      (apply str (repeat (dec num-chars) "-"))
                      (case percentage
                        0 " " 1 " " 2 "  " 98 "|" 99 "|" 100 "-|" 101 "-|" "|")
                      (->> (repeat (- width num-chars) " ") (apply str)))
        std-bar "|-------------------------|"
        poly-label (str " Poly " percentage "%")
        std-label "Std (100%)"
        scale-str "| 0%  | 25%  | 50% | 75%  | 100%"]
    (str std-bar "\n"
         poly-bar (if (< 99 percentage) "" "|") poly-label "\n"
         std-bar " " std-label "\n"
         scale-str)))

(defn parse-out-msecs [report]
  (->> report
       str
       reverse
       (drop 6)
       (take-while #(-> % (not= ",")))
       reverse
       rest
       (apply str)
       js/parseInt))

(defn get-average [form & [n]]
  (let [runs (take 10 (repeatedly #(parse-out-msecs (simple-benchmark [] (form) (or n 1000) :print-fn identity))))]
    (->> runs
         (apply +)
         (* 0.1)
         int)))

(defn data-bench-compare [title form-fn1 form-fn2 & [n]]
  (let [title (or title "Benchmark")
        res1 (get-average form-fn1 n)
        res2 (get-average form-fn2 n)
        av (int (* 100.0 (/ res1 res2)))
        chart (ascii-bar-chart av)
        description (str "poly-map is " av "% the speed of hash-map")
        in-macro? false]
    (println "\n### " title "\n")
    (when in-macro?
      (println "```clojure")
      (println form-fn2)
      (println "```\n"))
    (println "- Standard Map:" res1 "ms")
    (println "- Poly Map:" res2 "ms")
    (println "-" description "\n")
    (println "```clojure")
    (println chart)
    (println "```\n")
    {:description description
     :standard res1 :poly-map res2 :% av :chart chart}))

(defn -main [& _args]

  (data-bench-compare
    "Baseline Read: Large Map"
    #(get large-std-map (rand-key))
    #(get large-poly-map (rand-key))
    1000000)

  (data-bench-compare
    "Baseline Read: Missing Key"
    #(get large-std-map :not-a-key :default-val)
    #(get large-poly-map :not-a-key :default-val)
    1000000)

  (data-bench-compare
    "Baseline Write: Large Map Update"
    #(assoc large-std-map (rand-key) 999)
    #(assoc large-poly-map (rand-key) 999)
    1000000)

  (data-bench-compare
    "Baseline Reduce: Large Map Sum Values"
    #(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map)
    #(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-poly-map)
    1000)

  (data-bench-compare
    "Baseline Reduce: Large Map Sum Values"
    #(into {} (mapv vec (partition 2 large-map-data)))
    #(into poly/empty-poly-map (mapv vec (partition 2 large-map-data)))
    10)

  (data-bench-compare
    "Baseline Reduce: Large Map Sum Values"
    #(apply hash-map large-map-data)
    #(apply poly/poly-map large-map-data)
    100)

  (data-bench-compare
    "Override Impact: Simple Logging Assoc"
    #(assoc small-std-map :d 4)
    #(assoc logged-poly-map :d 4)
    1000000)

  (data-bench-compare
    "Override Impact: Simple Assoc"
    #(assoc small-std-map :d 4)
    #(assoc small-poly-map :d 4)
    1000000)

  (data-bench-compare
    "Override Impact: Validating Assoc - Valid Key"
    #(assoc large-std-map :new-key 123)
    #(assoc validated-poly-map :new-key 123)
    1000000)

  (data-bench-compare
    "Compare Baseline Assoc Large"
    #(assoc large-std-map :new-key 123)
    #(assoc large-poly-map :new-key 123)
    1000000)

  (data-bench-compare
    "Transient: Batch Assoc!"
    #(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient {}) items-to-add))
    #(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient poly/empty-poly-map) items-to-add))
    100)

  (data-bench-compare
    "Transient: persistent! Cost"
    #(persistent! (transient large-std-map))
    #(persistent! (transient large-poly-map))
    1000000)

  true)

#_(-main)