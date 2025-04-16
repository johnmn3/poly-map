# poly-map Examples

This document provides various examples demonstrating how `poly-map` can be used to create specialized map-like structures by overriding default behaviors.

## Setup

Most examples will require the following namespaces:

```clojure
(ns ex.user
  (:require
   [com.jolygon.poly-map.api-0
    :refer [poly-map empty-poly-map assoc-impl set-impls make-poly-map]]
   [com.jolygon.poly-map.api-0.keys :as pm]
   [com.jolygon.poly-map.api-0.trans.keys :as tpm]))
```

## 1. Default Values for Missing Keys

*Use Case*: You want a map that returns a specific default value (or `nil`) when a requested key is not found, instead of requiring the caller to provide a `nf` argument to `get`.

*How?*: Override `::pm/valAt_k` and `::pm/invoke-variadic`.

```clojure
(def default-value-map
  (-> empty-poly-map
      (assoc-impl
       ::pm/valAt_k
       (fn [_this m _impls _metadata k]
         ;; Check underlying map, return :not-available if truly missing
         (get m k :not-available))
       ::pm/invoke-variadic
       (fn [_this m _impls _metadata k & [not-available]]
         (get m k (or not-available :not-available))))))

(def m1 (assoc default-value-map :a 1))
```

### Example Usage:

```clojure
(get m1 :a)   ;=> 1
(m1 :a)       ;=> 1 (Arity-1 invoke defaults to get_k override)

(get m1 :b)   ;=> :not-available
(m1 :b)       ;=> :not-available
(m1 :b :soon) ;=> :soon
```

## 2. Case-Insensitive String Keys

*Use Case*: You need a map where string keys are treated case-insensitively (e.g., for HTTP headers).

*How?*: Override key lookup, association, and check impls to normalize string keys (e.g., to lowercase).

```clojure
(defn- normalize-key [k]
  (if (string? k) (.toLowerCase ^String k) k))

(def case-insensitive-map
  (-> empty-poly-map
      (assoc-impl
       ::pm/assoc_k_v
       (fn [_this m impls _metadata k v]
         ;; Construct new map using normalized key
         (make-poly-map (assoc m (normalize-key k) v) impls))
       ::pm/valAt_k
       (fn [_this m _impls _metadata k]
         (get m (normalize-key k)))
       ::pm/valAt_k_nf
       (fn [_this m _impls _metadata k nf]
         (get m (normalize-key k) nf))
       ::pm/containsKey_k
       (fn [_this m _impls _metadata k]
         (contains? m (normalize-key k)))
       ::pm/without_k
       (fn [_this m _impls _metadata k]
         (make-poly-map (dissoc m (normalize-key k)) impls)))))

(def headers
  (-> case-insensitive-map
      (assoc "Content-Type" "application/json")
      (assoc :other-header 123)))
```

### Example Usage:

```clojure
(get headers "content-type") ;=> "application/json"
(get headers "CONTENT-TYPE") ;=> "application/json"
(contains? headers "Content-type") ;=> true

(get headers :other-header) ;=> 123 (Non-string keys unaffected)

(dissoc headers "CONTENT-TYPE") ;=> {:other-header 123}
```

## 3. Schema Validation on Assoc

*Use Case*: Ensure that values associated with specific keys conform to a predefined schema (using `spec` in this example).

*How?*: Override `::pm/assoc_k_v` to perform validation before associating.

```clojure
(require '[clojure.spec.alpha :as s])

(s/def ::name string?)
(s/def ::age pos-int?)

(def schema-map
  (-> empty-poly-map
      (assoc-impl
       ::pm/assoc_k_v
       (fn [_this m impls metadata k v]
         (let [expected-type (case k :name ::name :age ::age :any)]
           (if (or (= expected-type :any) (s/valid? expected-type v))
             ;; Passes: Construct new map
             (make-poly-map (assoc m k v) impls metadata)
             ;; Fails: Throw exception
             (throw (ex-info "Schema validation failed"
                             {:key k :m v :expected (s/describe expected-type)}))))))))
```

### Example Usage:

```clojure
(def user (-> schema-map (assoc :name "Alice") (assoc :age 30)))
;=> {:name "Alice", :age 30}

(try
  (assoc user :age -5)
  (catch Exception e (ex-data e)))
;=> {:key :age, :value -5, :expected pos-int?}

(try
  (assoc user :name 123)
  (catch Exception e (ex-data e)))
;=> {:key :name, :value 123, :expected string?}
```

## 4. Logging Accesses (Read Logging)

*Use Case*: Track which keys are being read from the map, perhaps for debugging or analytics.

*How?*: Override `::pm/valAt_k` and `::pm/valAt_k_nf` to log the access.

```clojure
(def access-log (atom []))

(def logging-read-map
  (-> empty-poly-map
      (assoc-impl
       ::pm/valAt_k
       (fn [_this m _impls _metadata k]
         (swap! access-log conj [:get k])
         (get m k))
       ::pm/valAt_k_nf
       (fn [_this m _impls _metadata k nf]
         (swap! access-log conj [:get k nf])
         (get m k nf)))))

(def mlog (assoc logging-read-map :a 1))
```

### Example Usage:

```clojure
(reset! access-log [])
(get mlog :a)      ;=> 1
(get mlog :b)      ;=> nil (Logged as [:get :b])
(get mlog :c 404)  ;=> 404
@access-log
;=> [[:get :a] [:get :b] [:get :c 404]]
```

## 5. Side Effects on Update

*Use Case*: Trigger an external action (like notifying a UI component or saving to a DB) whenever the map is modified.

*How?*: Override `::pm/assoc_k_v` and `::pm/without_k`.

```clojure
(defn notify-change [change-type key value]
  (println "[Notification] Type:" change-type ", Key:" key ", Value:" value))

(def notifying-map
  (-> empty-poly-map
      (assoc-impl
       ::pm/assoc_k_v
       (fn assoc_k_v [_this m impls metadata k v]
         (notify-change :assoc k v) ; Trigger side effect
         ;; Construct and return new map, with recursive impl
         (make-poly-map (assoc m k v)
                        (assoc impls ::pm/assoc_k_v assoc_k_v
                        metadata)))
       ::pm/without_k
       (fn dissoc_k [_this m impls metadata k]
         (notify-change :dissoc k nil)
         ;; Construct and return new map, with recursive impl
         (make-poly-map (dissoc m k)
                        (assoc impls ::pm/without_k dissoc_k
                        metadata))))))
```

### Example Usage:

```clojure
(def nmap1 (assoc notifying-map :user "admin"))
; [Notification] Type: :assoc , Key: :user , Value: admin
(def nmap2 (dissoc nmap1 :user))
; [Notification] Type: :dissoc , Key: :user , Value: nil
```

## 6. Computed / Virtual Properties

*Use Case*: Define keys that don't store a static value but compute one based on other data in the map when accessed.

*How?*: Override `::pm/valAt_k` (and potentially `::pm/valAt_k_nf`).

```clojure
(def computed-prop-map
  (-> (poly-map :first-name "Jane" :last-name "Doe")
      (assoc-impl
       ::pm/valAt_k
       (fn [_this m _impls _metadata k]
         (if (= k :full-name)
           ;; Compute value for :full-name
           (str (:first-name m) " " (:last-name m))
           ;; Otherwise, standard lookup
           (get m k)))
       ;; Also override get with nf if needed
       ::pm/valAt_k_nf
       (fn [this m impls metadata k nf]
         (if (= k :full-name)
           ((::pm/valAt_k impls) this m impls metadata k) ; Delegate to above
           (get m k nf))))))
```

### Example Usage:

```clojure
(get computed-prop-map :first-name) ;=> "Jane"
(get computed-prop-map :full-name)  ;=> "Jane Doe"
(get computed-prop-map :age :unknown) ;=> :unknown
(get computed-prop-map :full-name :unknown)  ;=> "Jane Doe"
```

## 7. Lazy Loading from External Source

*Use Case*: Defer loading data for certain keys until they are actually requested, perhaps fetching from a database or file.

*How?*: Override `::pm/valAt_k_nf`. If the key isn't present, attempt to load it. This example also updates the map to cache the loaded value.

```clojure
(defn simulate-db-fetch [k]
  (println "[DB] Fetching data for key:" k)
  (Thread/sleep 50) ; Simulate delay
  (if (= k :user-prefs) {:theme "dark" :lang "en"} nil))

(def lazy-loading-map
  (-> empty-poly-map
      (assoc-impl
       ::pm/valAt_k_nf
       (fn [_this m _impls _metadata k nf]
         (let [val (get m k ::nf)]
           (if (= val ::nf)
             ;; Not found locally, try loading
             (if-let [loaded-val (simulate-db-fetch k)]
               ;; Found externally: assoc into a new map and return the value
               ;; This effectively caches the result.
               (do
                 (println "[Cache] Storing loaded value for key:" k)
                 ; To cache: override assoc_k_v as well.
                 loaded-val) ;; Simple version: just return loaded, no cache update
               ;; Not found externally either
               nf)
             ;; Found locally
             val)))
       ::pm/valAt_k
       (fn [this m impls metadata k]
         ((::pm/valAt_k_nf impls) this m impls metadata k ::nf))))) ; Delegate to above
```

### Example Usage (Simple Version - No Caching):

```clojure
(def lazy-map (assoc lazy-loading-map :config {:port 80}))

(get lazy-map :config) ;=> {:port 80} (No fetch)

(get lazy-map :user-prefs)
; [DB] Fetching data for key: :user-prefs
; [Cache] Storing loaded value for key: :user-prefs
;=> {:theme "dark", :lang "en"}

(get lazy-map :user-prefs) ; Access again
; [DB] Fetching data for key: :user-prefs (Fetched again as simple version doesn't cache)
; [Cache] Storing loaded value for key: :user-prefs
;=> {:theme "dark", :lang "en"}

(get lazy-map :other-key :default)
; [DB] Fetching data for key: :other-key
;=> :default
```

_(Note: Implementing caching correctly requires careful handling of returning new `PolyMap` instances from the override or coordinating between `get`/`valAt` and `assoc` overrides.)_

## 8. Read-Only Map View

*Use Case*: Provide a map interface to data that should not be modified through that interface.

*How?*: Override all mutating impls (`assoc`, `dissoc`, `conj`, etc.) to throw `UnsupportedOperationException`.

```clojure
(defn read-only-error [& _]
  (throw (UnsupportedOperationException. "Map is read-only")))

(def read-only-map-impls
  {::pm/assoc_k_v       read-only-error
   ::pm/dissoc_k        read-only-error
   ::pm/without_k        read-only-error
   ::pm/cons_o          read-only-error
   ::pm/assocEx_k_v     read-only-error
   ;; Override transient mutations too if you want `(transient read-only-map)` to fail
   ::tpm/assoc_k_v    read-only-error
   ::tpm/without_k    read-only-error
   ::tpm/conj_entry   read-only-error})

(def read-only-m
  (-> (poly-map :a 1)
      (set-impls read-only-map-impls)))
;; Or, to add to existing impls:
;; (def read-only-m
;;   (->> read-only-map-impls
;;        (apply assoc-impl (poly-map :a 1))))
```

### Example Usage:

```clojure
(get read-only-m :a) ;=> 1
(count read-only-m) ;=> 1

(try (assoc read-only-m :b 2) (catch Exception e (.getMessage e)))
;=> "Map is read-only"

(try (dissoc read-only-m :a) (catch Exception e (.getMessage e)))
;=> "Map is read-only"

;; Transient operations also fail if overridden
(try (persistent! (assoc! (transient read-only-m) :c 3)) (catch Exception e (.getMessage e)))
;=> "Map is read-only"
```

## 9. Function Call Dispatch

*Use Case*: Use the map itself as a dispatch mechanism, calling different functions based on arguments passed when the map is invoked.

*How?*: Override `::pm/invoke-variadic`.

```clojure
(defn handle-add [x y] (+ x y))
(defn handle-multiply [x y] (* x y))

(def dispatching-map
  (-> empty-poly-map
      (assoc :add-fn handle-add :mul-fn handle-multiply)
      (assoc-impl
       ::pm/invoke-variadic
       (fn [_this m _impls _metadata operation & args]
         (case operation
           :add (apply (:add-fn m) args)
           :multiply (apply (:mul-fn m) args)
           (throw (ex-info "Unknown operation" {:operation operation})))))))
```

### Example Usage:

```clojure
(dispatching-map :add 10 5)         ;=> 15
(dispatching-map :multiply 10 5)    ;=> 50

(try (dispatching-map :subtract 10 5) (catch Exception e (ex-data e)))
;=> {:operation :subtract}
```

## 10. Access Counting

*Use Case*: Keep track of how often keys are accessed.

*How?*: Override `::pm/valAt_k` and `::pm/valAt_k_nf`. Store counts in an atom external to the map.

```clojure
(def access-counts (atom {}))

(def counting-map
  (-> (poly-map :a 1 :b 2)
      (assoc-impl
       ::pm/valAt_k
       (fn [_this m _impls _metadata k]
         (swap! access-counts update k (fnil inc 0)) ; Increment count
         (get m k))
       ::pm/valAt_k_nf
       (fn [_this m _impls _metadata k nf]
         (swap! access-counts update k (fnil inc 0)) ; Increment count
         (get m k nf)))))
```

### Example Usage:

```clojure
(reset! access-counts {})
(get counting-map :a) ;=> 1
(get counting-map :b) ;=> 2
(get counting-map :a) ;=> 1
(get counting-map :c) ;=> nil
@access-counts
;=> {:a 2, :b 1, :c 1}
```

## 11. Transient Validation

*Use Case*: Perform validation efficiently during batch updates within a `transient`/`persistent!` block.

*How?*: Override transient impls like `::tpm/assoc_k_v`.

```clojure
(def transient-validating-map
  (-> empty-poly-map
      (assoc-impl
       ::tpm/assoc_k_v
       ;; Note: Needs access to the TransientPolyMap instance (`this`) to return it
       (fn [_this t-m impls metadata k v]
         (if (number? v)
           (poly/make-transient-poly-map (java.util.concurrent.atomic.AtomicBoolean. true)
                                         (assoc! t-m k v)
                                         impls
                                         metadata)
           (throw (ex-info "Transient validation failed: Value must be number" {:key k :value v})))))
      transient))
```

### Example Usage:

```clojure
;; Successful batch update
(persistent!
 (-> transient-validating-map
     (assoc! :x 10)
     (assoc! :y 20)))
;=> {:x 10, :y 20}

;; Failing batch update
(try
  (persistent!
   (-> (transient transient-validating-map)
       (assoc! :x 10)
       (assoc! :y "not a number"))) ; This will throw
  (catch Exception e (ex-data e)))
;=> {:key :y, :value "not a number"}
```

## 12. Custom String Representation

*Use Case*: Control how the map is printed or converted to a string, perhaps hiding sensitive data or providing a summary.

*How?*: Override `::pm/print-method_writer` and `::pm/toString`.

```clojure
(def sanitizing-string-map
  (-> (poly-map :user "secret-user" :id 123 :data [1 2 3])
      (assoc-impl
       ::pm/print-method_writer
       (fn [_this m _impls _metadata w]
         (doto w
           (.write "<SecureMapData id=")
           (.write (str (:id m)))
           (.write ">")))
       ::pm/toString
       (fn [_this m _impls _metadata]
         (str "<SecureMapData id=" (:id m) ">")))))
```

### Example Usage:

```clojure
(str sanitizing-string-map)
;=> "<SecureMapData id=123>"

(println sanitizing-string-map)
; <SecureMapData id=123>
```
