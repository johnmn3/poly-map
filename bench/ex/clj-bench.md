# CLJ `poly-map` Performance and Recent Improvements

This document outlines the performance of `poly-map`s in Clojure. If you're looking for the CLJS benchmarks [look here](./cljs-bench.md).

## Improvement

The initial release included some benchmark numbers. This table below shows how the performance of `poly-map`s have improved since then:

| Benchmark Operation |	Initial |	Later |	Overall Change (%) |
| ------- | ------- | ------- | ------- |
| Read Existing Key (Bench 1) |	98.9% |	98.8% |	-0.1% |
| Read Missing Key (Bench 2) | 55.4% | 88.2% | +59.2% |
| Write (Update Existing Key) (Bench 3) | 91.9% | 95.1% | +3.5% |
| Reduce (Sum Values) (Bench 4) | 99.7% | 91.3% | -8.4% |
| Construct (`into`) (Bench 5) | 55.1% | 92.0% | +67.0% |
| Construct (`apply`) (Bench 6) | 12.8% | 110.6% | +764.1% |
| Simple `assoc` (Baseline Poly - Bench 7) | 52.9% | 63.0% | +19.1% |
| Simple `assoc` (Logging Poly - Bench 7) | 16.4% | 38.5% | +134.8% |
| `assoc` New Key (Baseline Poly - Bench 8) | 5.35%* | 88.9% | +1561.7% |
| `assoc` New Key (Validated Poly - Bench 8) | 4.3%* | 74.5% | +1632.6% |
| Batch `assoc!` (Baseline Poly - Bench 9) | 33.9% | 96.1% | +183.5% |
| Batch `assoc!` (Logging Poly - Bench 9) | 53.5%* | 94.8% | +77.2% |
| `persistent!` Cost (Bench 10) | 31.0% | 45.2% | +45.8% |
| Contended Update (Illustrative - Bench 11) | 38.4% | 95.1% | +147.7% |

As you can see, except for very small maps and override scenarios, `poly-map`s are generally within 10% of the performance of stock Clojure `hash-map`s.

## Frontmatter

To get us started, here are some forms that will help us compare `poly-map`s and `hash-map`s.

```clojure
(ns ex.core-bench
  (:require
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
    (poly/assoc-impl (poly/poly-map :factor 2)
                     ::pm/invoke-variadic
                     (fn [_this m _impls _metadata x] (* (:factor m) x))))

  :end)
```

## Baseline Operations (Large Map - 10k elements)

### Read Existing Key (Bench 1):

```clojure
(get large-poly-map (rand-key))
```

  - Standard Map: 128.21 ns
  - Poly Map: 129.78 ns
  - Poly Map Speed: 98.8% of Standard

```clojure
|-------------------------| Poly (98.8%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Read Missing Key (Bench 2):

```clojure
(get large-poly-map :not-a-key :default-val)
```

- Standard Map: 13.66 ns
- Poly Map: 15.48 ns
- Poly Map Speed: 88.2% of Standard

```clojure
|----------------------|  | Poly (88.2%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Write (Update Existing Key) (Bench 3):

```clojure
(assoc large-poly-map (rand-key) 999)
```

- Standard Map: 192.15 ns
- Poly Map: 202.10 ns
- Poly Map Speed: 95.1% of Standard

```clojure
|------------------------|| Poly (95.1%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Reduce (Sum Values) (Bench 4):

```clojure
(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-poly-map)
```

- Standard Map: 176.11 µs
- Poly Map: 192.87 µs
- Poly Map Speed: 91.3% of Standard

```clojure
|-----------------------| | Poly (91.3%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Construct (into) (Bench 5):

```clojure
(into empty-poly-map (mapv vec (partition 2 large-map-data)))
```

- Standard Map: 6.91 ms
- Poly Map: 7.50 ms
- Poly Map Speed: 92.0% of Standard

```clojure
|-----------------------| | Poly (92.0%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Construct (apply) (Bench 6):

```clojure
(apply poly-map large-map-data)
```

- Standard Map: 1.77 ms
- Poly Map: 1.60 ms
- Poly Map Speed: 110.6% of Standard (Poly Faster!)

```clojure
|-----------------------------| Poly (110.6%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

## Override Impact

### Simple assoc (Small Map - Bench 7):

```clojure
(assoc small-poly-map :d 4)
```

- Standard Map: 35.87 ns
- Poly Map (Baseline): 56.93 ns -> 63.0% of Standard

```clojure
|----------------|        | Poly (Baseline - 63.0%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

```clojure
(assoc logged-poly-map :d 4)
```

- Poly Map (Logging): 93.17 ns -> 38.5% of Standard

```clojure
|---------|               | Poly (Logging - 38.5%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### assoc New Key (Large Map - Bench 8):

```clojure
(assoc large-poly-map :d 4)
```

- Standard Map: 96.43 ns
- Poly Map (Baseline): 108.39 ns -> 88.9% of Standard

```clojure
|----------------------|  | Poly (Baseline - 88.9%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

```clojure
(assoc validated-poly-map :d 4)
```

- Poly Map (Validated): 129.51 ns -> 74.5% of Standard

```clojure
|------------------|      | Poly (Validated - 74.5%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

## Transient Operations

### Batch assoc! (Large Map - Bench 9):

```clojure
(def items-to-add (vec (range large-map-size)))

(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient empty-poly-map) items-to-add))
```

- Standard Transient: 2.33 ms
- Poly Map Transient (Baseline): 2.42 ms -> 96.1% of Standard

```clojure
|------------------------|| Poly (Baseline - 96.1%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

```clojure
(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient logged-transient-map) items-to-add))
```

- Poly Map Transient (Logging): 2.46 ms -> 94.8% of Standard

```clojure
|------------------------|| Poly (Logging - 94.8%)
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### persistent! Cost (Large Map - Bench 10):

```clojure
(persistent! (transient large-poly-map))
```

- Standard Transient: 41.46 ns
- Poly Map Transient: 91.79 ns
- Poly Map Speed: 45.2% of Standard

```clojure
|-----------|             | Poly Persistent! (45.2%)
|-------------------------| Std Persistent! (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```

### Contended Update (Illustrative - Bench 11):

```clojure
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

(contended-poly-update 100)
```

- Standard Transient: 82.94 µs
- Poly Map Transient: 87.20 µs
- Poly Map Speed: 95.1% of Standard

```clojure
|------------------------|| Poly Contended (95.1%)
|-------------------------| Std Contended (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```
