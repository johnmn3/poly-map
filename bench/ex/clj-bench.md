# CLJ `wrap` Map Performance and Recent Improvements

This document outlines the performance of `wrap` maps in Clojure. If you're looking for the CLJS benchmarks [look here](./cljs-bench.md).

## Improvement

The initial release included some benchmark numbers. This table below shows how the performance of `wrap` maps have improved since then:

| Benchmark Operation |	Initial |	Later |	Overall Change (%) |
| ------- | ------- | ------- | ------- |
| Read Existing Key (Bench 1) |	98.9% |	98.8% |	-0.1% |
| Read Missing Key (Bench 2) | 55.4% | 88.2% | +59.2% |
| Write (Update Existing Key) (Bench 3) | 91.9% | 95.1% | +3.5% |
| Reduce (Sum Values) (Bench 4) | 99.7% | 91.3% | -8.4% |
| Construct (`into`) (Bench 5) | 55.1% | 92.0% | +67.0% |
| Construct (`apply`) (Bench 6) | 12.8% | 110.6% | +764.1% |
| Simple `assoc` (Baseline Wrap - Bench 7) | 52.9% | 63.0% | +19.1% |
| Simple `assoc` (Logging Wrap - Bench 7) | 16.4% | 38.5% | +134.8% |
| `assoc` New Key (Baseline Wrap - Bench 8) | 5.35%* | 88.9% | +1561.7% |
| `assoc` New Key (Validated Wrap - Bench 8) | 4.3%* | 74.5% | +1632.6% |
| Batch `assoc!` (Baseline Wrap - Bench 9) | 33.9% | 96.1% | +183.5% |
| Batch `assoc!` (Logging Wrap - Bench 9) | 53.5%* | 94.8% | +77.2% |
| `persistent!` Cost (Bench 10) | 31.0% | 45.2% | +45.8% |
| Contended Update (Illustrative - Bench 11) | 38.4% | 95.1% | +147.7% |

As you can see, except for very small maps and override scenarios, `wrap` maps are generally within 10% of the performance of stock Clojure `hash-map`s.

## Frontmatter

To get us started, here are some forms that will help us compare `wrap` maps and `hash-map`s.

```clojure
(ns ex.core-bench
  (:require
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

  (def invoke-override-map
    (w/assoc (wrap :factor 2)
             :invoke-variadic
             (fn [_ m x] (* (:factor m) x))))

  :end)
```

## Baseline Operations (Large Map - 10k elements)

### Read Existing Key (Bench 1):

```clojure
(get large-wrap-map (rand-key))
```

  - Standard Map: 128.21 ns
  - Wrap Map: 129.78 ns
  - Wrap Map Speed: 98.8% of Standard

```clojure
|-------------------------| Wrap (98.8%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Read Missing Key (Bench 2):

```clojure
(get large-wrap-map :not-a-key :default-val)
```

- Standard Map: 13.66 ns
- Wrap Map: 15.48 ns
- Wrap Map Speed: 88.2% of Standard

```clojure
|----------------------|  | Wrap (88.2%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Write (Update Existing Key) (Bench 3):

```clojure
(assoc large-wrap-map (rand-key) 999)
```

- Standard Map: 192.15 ns
- Wrap Map: 202.10 ns
- Wrap Map Speed: 95.1% of Standard

```clojure
|------------------------|| Wrap (95.1%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Reduce (Sum Values) (Bench 4):

```clojure
(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-wrap-map)
```

- Standard Map: 176.11 µs
- Wrap Map: 192.87 µs
- Wrap Map Speed: 91.3% of Standard

```clojure
|-----------------------| | Wrap (91.3%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Construct (into) (Bench 5):

```clojure
(into empty-wrap (mapv vec (partition 2 large-map-data)))
```

- Standard Map: 6.91 ms
- Wrap Map: 7.50 ms
- Wrap Map Speed: 92.0% of Standard

```clojure
|-----------------------| | Wrap (92.0%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Construct (apply) (Bench 6):

```clojure
(apply wrap large-map-data)
```

- Standard Map: 1.77 ms
- Wrap Map: 1.60 ms
- Wrap Map Speed: 110.6% of Standard (Wrap Faster!)

```clojure
|-----------------------------| Wrap (110.6%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

## Override Impact

### Simple assoc (Small Map - Bench 7):

```clojure
(assoc small-wrap-map :d 4)
```

- Standard Map: 35.87 ns
- Wrap Map (Baseline): 56.93 ns -> 63.0% of Standard

```clojure
|----------------|        | Wrap (Baseline - 63.0%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

```clojure
(assoc logged-wrap-map :d 4)
```

- Wrap Map (Logging): 93.17 ns -> 38.5% of Standard

```clojure
|---------|               | Wrap (Logging - 38.5%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### assoc New Key (Large Map - Bench 8):

```clojure
(assoc large-wrap-map :d 4)
```

- Standard Map: 96.43 ns
- Wrap Map (Baseline): 108.39 ns -> 88.9% of Standard

```clojure
|----------------------|  | Wrap (Baseline - 88.9%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

```clojure
(assoc validated-wrap-map :d 4)
```

- Wrap Map (Validated): 129.51 ns -> 74.5% of Standard

```clojure
|------------------|      | Wrap (Validated - 74.5%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

## Transient Operations

### Batch assoc! (Large Map - Bench 9):

```clojure
(def items-to-add (vec (range large-map-size)))

(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient empty-wrap) items-to-add))
```

- Standard Transient: 2.33 ms
- Wrap Map Transient (Baseline): 2.42 ms -> 96.1% of Standard

```clojure
|------------------------|| Wrap (Baseline - 96.1%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

```clojure
(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient logged-transient-map) items-to-add))
```

- Wrap Map Transient (Logging): 2.46 ms -> 94.8% of Standard

```clojure
|------------------------|| Wrap (Logging - 94.8%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### persistent! Cost (Large Map - Bench 10):

```clojure
(persistent! (transient large-wrap-map))
```

- Standard Transient: 41.46 ns
- Wrap Map Transient: 91.79 ns
- Wrap Map Speed: 45.2% of Standard

```clojure
|-----------|             | Wrap Persistent! (45.2%)
|-------------------------| Std Persistent! (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Contended Update (Illustrative - Bench 11):

```clojure
(def counter (atom 0))
(def contended-wrap-map
  (w/assoc empty-wrap
           :T_assoc_k_v
           (fn [_ t-m k v]
             (swap! counter inc)
             (assoc! t-m k v))))

(defn contended-wrap-update [n-updates]
  (reset! counter 0)
  (let [futures (doall (for [_ (range 10)] ; Simulate 10 threads
                         (future
                           (persistent!
                            (reduce (fn [t i] (assoc! t (keyword (str "k" i)) i))
                                    (transient contended-wrap-map)
                                    (range n-updates))))))]
    (run! deref futures))) ; Wait for all futures

(contended-wrap-update 100)
```

- Standard Transient: 82.94 µs
- Wrap Map Transient: 87.20 µs
- Wrap Map Speed: 95.1% of Standard

```clojure
|------------------------|| Wrap Contended (95.1%)
|-------------------------| Std Contended (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```
