Measuring ex.core-bench

### Frontmatter

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
  (def large-poly-map (into empty-poly-map large-std-map))
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

## baseline-read-large-standard-map
```clojure
(get large-std-map (rand-key))
```
* time: 22.785000 µs, sd: 56.325663 µs
### baseline-read-large-poly-map
```clojure
(get large-poly-map (rand-key))
```
* time: 18.674700 µs, sd: 22.829404 µs

_poly is 122.06% the speed of standard, or_
_standard is 77.94% the speed of poly_

    |------------------------------------------------| 100% poly-map
    |-------------------------------------| 77.94% regular map

## baseline-read-large-standard-map
```clojure
(get large-std-map :not-a-key :default-val)
```
* time: 119.302140 ns, sd: 0.000000 ns
### baseline-read-large-poly-map
```clojure
(get large-poly-map :not-a-key :default-val)
```
* time: 87.455270 ns, sd: 0.000000 ns

_poly is 136.4% the speed of standard, or_
_standard is 63.6% the speed of poly_

    |-------------------------------------------------| 100% poly-map
    |------------------------------| 63.6% regular map

## baseline-read-missing-key-standard
```clojure
(get large-std-map :not-a-key :default-val)
```
* time: 14.740737 ns, sd: 0.000000 ns
### baseline-read-missing-key-poly
```clojure
(get large-poly-map :not-a-key :default-val)
```
* time: 14.588405 ns, sd: 0.000000 ns

_poly is 101.03% the speed of standard, or_
_standard is 98.97% the speed of poly_

    |------------------------------------------------| 100% poly-map
    |-----------------------------------------------| 98.97% regular map

## baseline-write-large-map-update-standard
```clojure
(assoc large-std-map (rand-key) 999)
```
* time: 182.456647 ns, sd: 0.000000 ns
### baseline-write-large-map-update-poly
```clojure
(assoc large-poly-map (rand-key) 999)
```
* time: 654.924066 ns, sd: 0.000000 ns

_poly is 27.85% the speed of standard_

    |____________| 27.85% poly-map
    |------------------------------------------------| 100% regular map

## baseline-reduce-large-map-sum-values-standard
```clojure
(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map)
```
* time: 98.503084 µs, sd: 0.002084 ns
### baseline-reduce-large-map-sum-values-poly
```clojure
(reduce-kv (fn [acc _ v] (+ acc v)) 0 large-poly-map)
```
* time: 159.783483 ns, sd: 0.000000 ns

_poly is 61.65% the speed of standard_

    |----------------------------| 61.65% poly-map
    |------------------------------------------------| 100% regular map

## baseline-construct-large-map-into-standard
```clojure
(def large-map-data (vec (mapcat (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))

(into {} (mapv vec (partition 2 large-map-data))
```
* time: 6.030740 ms, sd: 22.856592 ns
### baseline-construct-large-map-into-poly
```clojure
(into empty-poly-map (mapv vec (partition 2 large-map-data))
```
* time: 10.361717 ms, sd: 76.730260 ns

_poly is 58.2% the speed of standard_

    |---------------------------| 58.2% poly-map
    |------------------------------------------------| 100% regular map

## baseline-construct-large-map-apply-standard
```clojure
(apply hash-map large-map-data)
```
* time: 1.328213 ms, sd: 0.356873 ns
### baseline-construct-large-map-apply-poly
```clojure
(apply poly-map large-map-data)
```
* time: 10.391062 ms, sd: 48.752624 ns

_poly is 12.8% the speed of standard_

    |----| 12.8% poly-map
    |------------------------------------------------| 100% regular map

We need to optimize `apply`.

## override-impact-simple-assoc-standard
```clojure
(assoc small-std-map :d 4)
```
* time: 29.508727 ns, sd: 0.000000 ns
### override-impact-simple-assoc-poly
```clojure
(assoc small-poly-map :d 4)
```
* time: 40.261695 ns, sd: 0.000000 ns
_poly is 73.3% the speed of standard_
### override-impact-simple-logging-assoc-poly
```clojure
(assoc logged-poly-map :d 4)
```
* time: 179.260320 ns, sd: 0.000000 ns

_logging poly is 16.4% the speed of standard_

    |------| 16.4% logging poly-map
    |-----------------------------------| 73.3% poly-map
    |------------------------------------------------| 100% regular map

## override-impact-large-assoc-new-key-standard
```clojure
(assoc small-std-map :d 4) ; again
```
* time: 29.544054 ns, sd: 0.000000 ns
### override-impact-large-assoc-new-key-poly
```clojure
(assoc large-poly-map :d 4)
```
* time: 551.991061 ns, sd: 0.000000 ns
_poly is 5.35% the speed of standard_
### override-impact-large-validated-assoc-new-key-poly
```clojure
(assoc validated-poly-map :d 4)
```
* time: 686.209108 ns, sd: 0.000000 ns

_validated poly is 4.3% the speed of standard_

    || 4.3% validated poly-map
    |-| 5.35% poly-map
    |------------------------------------------------| 100% regular map

This might be a bug. Super slow.

## transient-batch-assoc!-standard
```clojure
(def items-to-add (vec (range large-map-size)))

(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient {}) items-to-add))
```
* time: 2.146258 ms, sd: 3.514564 ns
### transient-batch-assoc!-poly
```clojure
(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient empty-poly-map) items-to-add))
```
* time: 6.340604 ms, sd: 95.930049 ns
_transient poly is 33.91% the speed of standard transient_
### transient-batch-assoc!-logging-poly
```clojure
(def logged-transient-map
  (-> empty-poly-map
      (poly/assoc-impl
       ::tpm/assoc_k_v
       (fn [_this t-m impls metadata k v]
         (swap! log-atom inc)
         (poly/make-transient-poly-map (java.util.concurrent.atomic.AtomicBoolean. true) (assoc! t-m k v) impls metadata)))))

(persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient logged-transient-map) items-to-add))
```
* time: 4.018140 ms, sd: 12.327567 ns

_transient logging poly is 53.48% the speed of standard transient_

    |--------------| 33.91% logging transient poly-map
    |-------------------------| 53.48% transient poly-map
    |------------------------------------------------| 100% transient map

## transient-persistent!-cost-standard
```clojure
(persistent! (transient large-std-map))
```
* time: 41.295457 ns, sd: 0.000000 ns
### transient-persistent!-cost-poly
```clojure
(persistent! (transient large-poly-map))
```
* time: 133.183714 ns, sd: 0.000000 ns

_poly is 31.01% the speed of standard_

    |--------------| 31.01% transient poly-map
    |------------------------------------------------| 100% transient map

## transient-contended-standard
```clojure
(def counter (atom 0))
(def contended-poly-map
  (poly/assoc-impl poly/empty-poly-map
                   ::tpm/assoc_k_v
                   (fn [this t-m impls metadata k v]
                     (swap! counter inc) ; Shared mutable state
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

(contended-std-update 100)
```
* time: 80.872325 µs, sd: 0.016305 ns
### transient-contented-poly
```clojure
(contended-poly-update 100)
```
* time: 210.668691 µs, sd: 0.006115 ns

_poly is 38.39% the speed of standard_

    |-----------------| 38.39% transient contended poly-map
    |------------------------------------------------| 100% transient contended map
