# CLJS `wrap` Map Performance and Recent Improvements

This document outlines the performance of `wrap` maps in ClojureScript (tested on Node.js), reflecting optimizations including multi-deftype implementation and the use of `defrecord` for implementation maps. If you're looking for the Clojure benchmarks [look here](./clj-bench.md).

## Improvement Summary (Run 1 vs Run 8)

The initial results were recorded before major architectural changes. Significant optimizations were made leading up to the latest benchmarks. This table shows the relative performance improvement between these runs. I Gemini collate the numbers into this table.

**Relative Speed % = (Standard Map Time / Wrap Map Time) * 100%**
**Improvement % = `((Speed % Run 8 / Speed % Run 1) - 1) * 100%`**

| Benchmark Operation                      | Original Speed (%) | Speed Now (%) | Overall Change (%) |
| :--------------------------------------- | :-------------- | :-------------- | :----------------- |
| Read Existing Key (Large Map)            | 57.5%           | 80%             | **+39.1%** |
| Read Missing Key                         | 51.0%           | 66%             | **+29.4%** |
| Write (Update Existing Key - Large Map)  | 52.7%           | 92%             | **+74.6%** |
| Reduce (Sum Values - Large Map)        | 107.8%          | 97%             | **-10.0%** |
| Simple `assoc` (Baseline Wrap - Small) | 29.0%           | 102%            | **+251.7%** |
| Simple `assoc` (Logging Wrap - Small)  | 28.9%           | 100%            | **+246.0%** |
| `assoc` New Key (Baseline Wrap - Large)| 52.5%           | 96%             | **+82.9%** |
| `assoc` New Key (Validated Wrap - Large)| 53.3%           | 92%             | **+72.6%** |
| Batch `assoc!` (Baseline Wrap)         | 77.7%           | 97%             | **+24.8%** |
| Batch `assoc!` (Logging Wrap)          | 77.6%           | 97%*            | **+25.0%** | 
| `persistent!` Cost                       | 13.2%           | 48%             | **+263.6%** |

As shown, massive improvements were achieved, particularly for `assoc` operations which now often meet or exceed standard map performance. Transient batch operations are also highly competitive. The main remaining bottleneck relative to standard maps in CLJS is the `persistent!` cost.

## Frontmatter

Setup code similar to the Clojure benchmarks is used.

```clojure
(ns ex.cljs-bench-setup ;; Example namespace
  (:require
   [com.jolygon.wrap-map :as w :refer [wrap empty-wrap freeze]]))

(do
  ;; baseline
  (def small-std-map {:a 1 :b 2 :c 3})
  (def small-wrap-map (wrap :a 1 :b 2 :c 3))
  (def frozen-small-wrap-map (w/freeze small-wrap-map))
  (def large-map-size 10000)
  (def large-std-map (into {} (mapv (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))
  (def large-wrap-map (doall (into w/empty-wrap large-std-map)))
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
  (def logged-wrap-map (w/assoc small-wrap-map :assoc_k_v logging-assoc-impl))
  (def frozen-logged-wrap-map (w/freeze logged-wrap-map))
  (def validated-wrap-map (w/assoc large-wrap-map :assoc_k_v validating-assoc-impl))
  (def frozen-validated-wrap-map (w/freeze validated-wrap-map))
  (def large-map-data (vec (mapcat (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))
  (def items-to-add (vec (range large-map-size)))
  :end)
  ```

## Baseline Operations (Large Map - 10k elements)

### Baseline Read: Large Map

```clojure
(get large-wrap-map (rand-key))
```

- Standard Map: 151 ms
- Wrap Map: 188 ms -> *80% of Standard*
- Frozen Wrap: 274 ms -> *67% of Standard*

```clojure
|--------------------|      | Wrap (80%)
|----------------|        | Frozen Wrap (67%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

### Baseline Read: Missing Key

```clojure
(get large-wrap-map :not-a-key :default-val)
```

- Standard Map: 94 ms
- Wrap Map: 141 ms -> *66% of Standard*
- Frozen Wrap: 140 ms -> *65% of Standard*

|----------------|        | Wrap (66%)
|---------------|         | Frozen Wrap (65%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%

### Baseline Write: Large Map Update

```clojure
(assoc large-wrap-map (rand-key) 999)
```

- Standard Map: 560 ms
- Wrap Map: 606 ms -> *92% of Standard*
- Frozen Wrap: 438 ms -> *102% of Standard* (Frozen Faster!)

```clojure
|----------------------|  | Wrap (92%)
|-------------------------| Frozen Wrap (102%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

### Baseline Reduce: Large Map Sum Values

```clojure
(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-wrap-map)
```

- Standard Map: 274 ms
- Wrap Map: 280 ms -> *97% of Standard*
- Frozen Wrap: 276 ms -> *98% of Standard*

```clojure
|-----------------------| | Wrap (97%)
|------------------------| Wrap (98%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

### Baseline Into: Large Map Construction

```clojure
(into w/empty-wrap (mapv vec (partition 2 large-map-data)))
```

- Standard Map: 286 ms
- Wrap Map: 301 ms -> *95% of Standard*
- Frozen Wrap: 304 ms -> *91% of Standard*

```clojure
|-----------------------| | Wrap (95%)
|----------------------|  | Frozen Wrap (91%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

### Baseline Apply: Large Map Construction

```clojure
(apply w/wrap large-map-data)
```

- Standard Map: 229 ms
- Wrap Map: 292 ms -> *78% of Standard*

```clojure
|-------------------|     | Wrap (78%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

_(Note: No frozen apply constructor tested)_

## Override Impact

### Override Impact Baseline: Simple Assoc

```clojure
(assoc small-wrap-map :d 4)
```

- Standard Map: 128 ms
- Wrap Map (Baseline): 125 ms -> *102% of Standard* (Wrap Faster!)

```clojure
|-------------------------| Wrap (Baseline - 102%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

### Override Impact: Simple Logging Assoc

```clojure
(assoc logged-wrap-map :d 4)
```

- Standard Map: 123 ms
- Wrap Map (Logging): 122 ms -> *100% of Standard*
- Frozen Wrap (Logging): 125 ms -> *102% of Standard* (Frozen Faster!)

```clojure
|-------------------------| Wrap (Logging - 100%)
|-------------------------| Frozen Wrap (Logging - 102%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

### Override Impact: Validating Assoc - Valid Key

```clojure
(assoc validated-wrap-map :new-key 123)
```

- Standard Map: 306 ms
- Wrap Map (Validated): 331 ms -> *92% of Standard*
- Frozen Wrap (Validated): 326 ms -> *98% of Standard*

```clojure
|----------------------|  | Wrap (Validated - 92%)
|------------------------| Frozen Wrap (Validated - 98%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

### Compare Baseline Assoc Large (Assoc New Key)

```clojure
(assoc large-wrap-map :new-key 123)
```

- Standard Map: 315 ms
- Wrap Map (Baseline): 326 ms -> *96% of Standard*
- Frozen Wrap (Baseline): 330 ms -> *95% of Standard*

```clojure
|-----------------------| | Wrap (Baseline - 96%)
|-----------------------| | Frozen Wrap (Baseline - 95%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

## Transient Operations

### Transient: Batch Assoc!

```clojure
(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient w/empty-wrap) items-to-add))
```

- Standard Transient: 663 ms
- Wrap Map Transient (Baseline): 681 ms -> *97% of Standard*
- Frozen Wrap Transient (Baseline): 694 ms -> *95% of Standard*

```clojure
|-----------------------| | Wrap (Baseline - 97%)
|-----------------------| | Frozen Wrap (Baseline - 95%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```

### Transient: persistent! Cost

```clojure
(persistent! (transient large-wrap-map))
```

- Standard Transient: 25 ms
- Wrap Map Transient (Baseline): 52 ms -> *48% of Standard*
- Frozen Wrap Transient (Baseline): 68 ms -> *52% of Standard*

```clojure
|-----------|             | Wrap (Baseline - 48%)
|------------|            | Frozen Wrap (Baseline - 52%)
|-------------------------| Std (100%)
0%   25%   50%   75%  100%
```
