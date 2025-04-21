(ns ex.cljs-bench
  (:require
    [com.jolygon.wrap-map :as w :refer [wrap]]))

;; (set-print-fn! println)

(println :starting :ex.cljs-bench)

;; baseline
(def small-std-map {:a 1 :b 2 :c 3})
(def small-wrap-map (wrap :a 1 :b 2 :c 3))
(def large-map-size 10000)
(def large-std-map (into {} (mapv (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))
(def large-wrap-map (into w/empty-wrap large-std-map))
(def frozen-large-wrap-map (w/freeze large-wrap-map))
(def keys-to-access (vec (keys large-std-map)))
(defn rand-key [] (rand-nth keys-to-access))
;; Overrides
(def log-atom (atom 0))
(defn logging-assoc-impl [{:as e :keys [<-]} m k v]
  (swap! log-atom inc)
  (<- e (assoc m k v)))
(defn validating-assoc-impl [{:as e :keys [<-]} m k v]
  (if (keyword? k)
    (<- e (assoc m k v))
    (throw (ex-info "Invalid key" {:key k}))))
(def logged-wrap-map (w/assoc small-wrap-map :-assoc_k_v logging-assoc-impl))
(def frozen-logged-wrap-map (w/freeze logged-wrap-map))
(def validated-wrap-map (w/assoc large-wrap-map :-assoc_k_v validating-assoc-impl))
(def frozen-validated-wrap-map (w/freeze validated-wrap-map))
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
        wrap-bar (str "|"
                      (apply str (repeat (dec num-chars) "-"))
                      (case percentage
                        0 " " 1 " " 2 "  " 98 "|" 99 "|" 100 "-|" 101 "-|" "|")
                      (->> (repeat (- width num-chars) " ") (apply str)))
        std-bar "|-------------------------|"
        wrap-label (str " Wrap " percentage "%")
        std-label "Std (100%)"
        scale-str "| 0%  | 25%  | 50% | 75%  | 100%"]
    (str std-bar "\n"
         wrap-bar (if (< 99 percentage) "" "|") wrap-label "\n"
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
        description (str "wrap map is " av "% the speed of hash-map")
        in-macro? false]
    (println "\n### " title "\n")
    (when in-macro?
      (println "```clojure")
      (println form-fn2)
      (println "```\n"))
    (println "- Standard Map:" res1 "ms")
    (println "- Wrap Map:" res2 "ms")
    (println "-" description "\n")
    (println "```clojure")
    (println chart)
    (println "```\n")
    {:description description
     :standard res1 :wrap-map res2 :% av :chart chart}))

(def frozen-empty-wrap (w/freeze w/empty-wrap))

(defn -main [& _args]

  (data-bench-compare
    "Baseline Read: Large Map"
    #(get large-std-map (rand-key))
    #(get large-wrap-map (rand-key))
    1000000)

  (data-bench-compare
    "Frozen Baseline Read: Large Map"
    #(get large-std-map (rand-key))
    #(get frozen-large-wrap-map (rand-key))
    1000000)

  (data-bench-compare
    "Baseline Read: Missing Key"
    #(get large-std-map :not-a-key :default-val)
    #(get large-wrap-map :not-a-key :default-val)
    1000000)

  (data-bench-compare
    "Frozen Baseline Read: Missing Key"
    #(get large-std-map :not-a-key :default-val)
    #(get frozen-large-wrap-map :not-a-key :default-val)
    1000000)

  (data-bench-compare
    "Baseline Write: Large Map Update"
    #(assoc large-std-map (rand-key) 999)
    #(assoc large-wrap-map (rand-key) 999)
    1000000)

  (data-bench-compare
    "Frozen Baseline Write: Large Map Update"
    #(assoc large-std-map (rand-key) 999)
    #(assoc frozen-large-wrap-map (rand-key) 999)
    1000000)

  (data-bench-compare
    "Baseline Reduce: Large Map Sum Values"
    #(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map)
    #(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-wrap-map)
    1000)

  (data-bench-compare
    "Frozen Baseline Reduce: Large Map Sum Values"
    #(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map)
    #(reduce-kv (fn [acc _ v] (+ acc v)) 0 frozen-large-wrap-map)
    1000)

  (data-bench-compare
    "Baseline Into: Large Map Sum Values"
    #(into {} (mapv vec (partition 2 large-map-data)))
    #(into w/empty-wrap (mapv vec (partition 2 large-map-data)))
    10)

  (let [f-empty-wrap (w/freeze w/empty-wrap)]
    (data-bench-compare
      "Frozen Baseline Into: Large Map Sum Values"
      #(into {} (mapv vec (partition 2 large-map-data)))
      #(into f-empty-wrap (mapv vec (partition 2 large-map-data)))
      10))

  (data-bench-compare
    "Baseline Apply: Large Map Sum Values"
    #(apply hash-map large-map-data)
    #(apply w/wrap large-map-data)
    100)

  ;; direct constructor for frozen
  (println "")
  (println "No frozen apply constructor")
  (println "")

  (data-bench-compare
    "Override Impact Baseline: Simple Assoc"
    #(assoc small-std-map :d 4)
    #(assoc small-wrap-map :d 4)
    1000000)

  (data-bench-compare
    "Override Impact: Simple Logging Assoc"
    #(assoc small-std-map :d 4)
    #(assoc logged-wrap-map :d 4)
    1000000)

  (data-bench-compare
    "Frozen Override Impact: Simple Logging Assoc"
    #(assoc small-std-map :d 4)
    #(assoc frozen-logged-wrap-map :d 4)
    1000000)

  (data-bench-compare
    "Override Impact: Validating Assoc - Valid Key"
    #(assoc large-std-map :new-key 123)
    #(assoc validated-wrap-map :new-key 123)
    1000000)

  (data-bench-compare
    "Frozen Override Impact: Validating Assoc - Valid Key"
    #(assoc large-std-map :new-key 123)
    #(assoc frozen-validated-wrap-map :new-key 123)
    1000000)

  (data-bench-compare
    "Compare Baseline Assoc Large"
    #(assoc large-std-map :new-key 123)
    #(assoc large-wrap-map :new-key 123)
    1000000)

  (data-bench-compare
    "Frozen Compare Baseline Assoc Large"
    #(assoc large-std-map :new-key 123)
    #(assoc frozen-large-wrap-map :new-key 123)
    1000000)

  (data-bench-compare
    "Transient: Batch Assoc!"
    #(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient {}) items-to-add))
    #(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient w/empty-wrap) items-to-add))
    100)

  (data-bench-compare
    "Frozen Transient: Batch Assoc!"
    #(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient {}) items-to-add))
    #(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient frozen-empty-wrap) items-to-add))
    100)

  (data-bench-compare
    "Transient: persistent! Cost"
    #(persistent! (transient large-std-map))
    #(persistent! (transient large-wrap-map))
    1000000)

  (data-bench-compare
    "Frozen Transient: persistent! Cost"
    #(persistent! (transient large-std-map))
    #(persistent! (transient frozen-large-wrap-map))
    1000000)

  true)

#_(-main)
