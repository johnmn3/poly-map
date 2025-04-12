(ns ex.cljs-bench
  (:require
   [com.jolygon.poly-map.api-0 :as poly :refer [poly-map]]
   [com.jolygon.poly-map.api-0.keys :as pm]
   [com.jolygon.poly-map.api-0.trans.keys :as tpm]))

;; load up dep data
(do
  (def small-std-map {:a 1 :b 2 :c 3})
  (def small-poly-map (poly-map :a 1 :b 2 :c 3))
  (def large-map-size 10000)
  (def large-std-map (into {} (mapv (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))
  (def large-poly-map (doall (into poly/empty-poly-map large-std-map)))
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
                     (fn [_this m _impls _metadata x] (* (:factor m) x)))))

;;;;;;


;; --- Category 1: Baseline Overhead ---

;; Area 1.1: Read Operations (Large Map Existing Key)

(println "Baseline Read: Large Map (std)")
(simple-benchmark [] (get large-std-map (rand-key)) 1000000)
; 203 msecs
(println "Baseline Read: Large Map (poly)")
(simple-benchmark [] (get large-poly-map (rand-key)) 1000000)
; 353 msecs

;; poly 57.51% of std

(println "Bench Baseline Read: Large Map (std)")
(simple-benchmark [] (get large-std-map (rand-key)) 1000000)
; 223 msecs
(println "Bench Baseline Read: Large Map (poly)")
(simple-benchmark [] (get large-poly-map (rand-key)) 1000000)
;  377 msecs

;; poly 59.15% of std

;; Area 1.1: Read Operations (Missing Key with default)

(println "Baseline Read: Missing Key (std)")
(simple-benchmark [] (get large-std-map :not-a-key :default-val) 1000000)
; 102 msecs
(println "Baseline Read: Missing Key (poly)")
(simple-benchmark [] (get large-poly-map :not-a-key :default-val) 1000000)
; 200 msecs

;; poly 51% of std

;; Area 1.2: Write Operations (Large Map Update)

(println "Baseline Write: Large Map Update (std)")
(simple-benchmark [] (assoc large-std-map (rand-key) 999) 1000000)
; 548 msecs
(println "Baseline Write: Large Map Update (poly)")
(simple-benchmark [] (assoc large-poly-map (rand-key) 999) 1000000)
; 1040 msecs

;; poly 52.69% of std


;; Area 1.3: Iteration/Reduction (Large Map)
(println "Baseline Reduce: Large Map Sum Values (std)")
(println :res-std (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map))
(simple-benchmark [] (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-std-map) 1000)
; 332 msecs
(println "Baseline Reduce: Large Map Sum Values (poly)")
(println :res (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-poly-map))
(simple-benchmark [] (reduce-kv (fn [acc _ v] (+ acc v)) 0 large-poly-map) 1000)
; 308 msecs

;; std 92.2% of poly
;; poly took the lead!

;; Area 1.4: Construction (Large Map)

(def large-map-data (vec (mapcat (fn [i] [(keyword (str "k" i)) i]) (range large-map-size))))
(println "Baseline Construct: Large Map (std - into)")
(simple-benchmark [] (into {} (mapv vec (partition 2 large-map-data))) 100)
; 2852 msecs
(println "Baseline Construct: Large Map (poly - into)")
(simple-benchmark [] (into poly/empty-poly-map (mapv vec (partition 2 large-map-data))) 100)
; 3042 msecs

;; poly 93.75% of poly
;; close! - good use of transients for `into` here.

(println "Baseline Construct: Large Map (std - std-map)")
(simple-benchmark [] (apply hash-map large-map-data) 100)
; 307 msecs
(println "Baseline Construct: Large Map (poly - poly-map)")
(simple-benchmark [] (apply poly/poly-map large-map-data) 100)
; 3147 msecs

;; poly 9.76% of std
;; huge 10x drop off - our apply impl needs optimization

;; --- Category 2: Override Impact ---

;; Area 2.1: Simple Overrides (Logging Assoc)

(println "Override Impact: Simple Assoc (std)")
(simple-benchmark [] (assoc small-std-map :d 4) 1000000)
; 145 msecs
(println "Override Impact: Simple Logging Assoc (poly)")
(simple-benchmark [] (assoc logged-poly-map :d 4) 1000000)
; 502 msecs

;; poly 28.88% of std

(simple-benchmark [] (assoc small-poly-map :d 4) 1000000)
; 500 msecs

;; poly 29% of std

;; hmm, the logging had almost zero overhead here, but assoc stayed 70% slower. For larger maps, polys are only 2x slower.


(println "Assoc -  Key (std)")
(simple-benchmark [] (assoc large-std-map :new-key 123) 1000000)
; 417 msecs
(println "Override Impact: Validating Assoc - Valid Key (poly)")
(simple-benchmark [] (assoc validated-poly-map :new-key 123) 1000000)
; 783 msecs
;; poly 53.26% of std
(println "Compare Baseline Assoc Large (poly)")
(simple-benchmark [] (assoc large-poly-map :new-key 123) 1000000)
; 795 msecs
;; poly 52.45% of std

;; again, the added behavior has minimal overhead in cljs - the abstraction is most of the cost.

;; Area 2.3: Invoke Override
(println "Override Impact: Invoke Override (poly)")
(simple-benchmark [] (invoke-override-map 10) 1000000) ; Calls the override
; 690 msecs

(println "Compare Baseline Lookup (poly)")
(simple-benchmark [] (get (poly/poly-map :factor 2) :factor) 1000000) ; Standard lookup
; 2954 msecs

;; poly 23.36% of std

;; --- Category 3: Transient Performance ---

;; Area 3.1: Batch Updates (Assoc!)
(def items-to-add (vec (range large-map-size)))
(println "Transient: Batch Assoc! (std)")
(simple-benchmark [] (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient {}) items-to-add)) 100)
; 691 msecs
(println "Transient: Batch Assoc! (poly)")
(simple-benchmark [] (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient poly/empty-poly-map) items-to-add)) 100)
; 889 msecs

;; poly 77.73% of std

;; Area 3.1: Batch Updates (Assoc! with Logging Override)

(def logged-transient-map
  (poly/assoc-impl poly/empty-poly-map
                   ::tpm/assoc_k_v
                   (fn [_this t-m impls metadata k v]
                     (swap! log-atom inc) ; Simple transient override action
                     (poly/make-transient-poly-map true (assoc! t-m k v) impls metadata))))
(println "Transient: Batch Assoc! with Logging Override (poly)")
(simple-benchmark [] (persistent! (reduce (fn [t i] (assoc! t (keyword (str "new" i)) i)) (transient logged-transient-map) items-to-add)) 100)
; 890 msecs

;; poly 77.64% of std

;; Again, in cljs, adding behaviors like swapping to an external atom introduce nearly no overhead. Again, only a quarter slower than stock.

;; So, good performance while in transient mode.

;; Area 3.3: persistent! Transition Cost

(println "Transient: persistent! Cost (std)")
(simple-benchmark [] (persistent! (transient large-std-map)) 1000000)
; 70 msecs
(println "Transient: persistent! Cost (poly)")
(simple-benchmark [] (persistent! (transient large-poly-map)) 1000000)
; 529 msecs

;; poly 13.23% of std

;; huge slow down on transient/persist slowdown - let's see if we can optimize there.

;; Area 3.4: Contention Scenario (Illustrative - Requires override mutating shared state)
;; Note: this was copied over from CLJ. There's no contention in the same sense here.
;;   Using cljs-thread may provide some contention scenario, but that needs to be ported
;;   to nodejs and SAB based blocking, where we'd be measuring SAB atomics contention.

;; Create a poly map where assoc! updates a shared atom counter
(def counter (atom 0))
(def contended-poly-map
  (poly/assoc-impl poly/empty-poly-map
                   ::tpm/assoc_k_v
                   (fn [_this t-m impls metadata k v]
                     (swap! counter inc) ; Shared mutable state
                     (poly/make-transient-poly-map true (assoc! t-m k v) impls metadata)
                     #_this)))

(defn contended-poly-update [n-updates]
  (doall
   (for [_ (range 10)] ; Simulate 10 threads
     (persistent!
      (reduce (fn [t i] (assoc! t (keyword (str "k" i)) i))
              (transient contended-poly-map)
              (range n-updates))))))

(def contended-std-map {})

(defn contended-std-update [n-updates]
  (doall
   (for [_ (range 10)] ; Simulate 10 threads
     (persistent!
      (reduce (fn [t i] (assoc! t (keyword (str "k" i)) i))
              (transient contended-std-map)
              (range n-updates)))))) ; Wait for all futures

;; note, there's no contention on cljs, in a threadlike way, so this is not measuring actual contention
(println "Transient: Contended Standard Update via Atom (Illustrative)")
(simple-benchmark [] (contended-std-update 100) 1000)
; 566 msecs
(simple-benchmark [] (contended-std-update 1000) 100)
; 731 msecs
(simple-benchmark [] (contended-std-update 10000) 10)
; 942 msecs

(println "Transient: Contended Poly Update via Atom (Illustrative)")
(simple-benchmark [] (contended-poly-update 100) 1000)
; 700 msecs
; 80.86%
(simple-benchmark [] (contended-poly-update 1000) 100)
; 845 msecs
; 86.51%
(simple-benchmark [] (contended-poly-update 10000) 10)
; 1051 msecs
; 89.63%

;; overhead goes down as batch transient operations become larger