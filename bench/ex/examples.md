# `wrap` Map Examples

This document provides various examples demonstrating how _`wrap` maps_ can be used to create specialized map-like structures by overriding default behaviors.

## Setup

Most examples will require the following namespaces:

```clojure
(ns ex.examples-low-level-md
  (:require
    [com.jolygon.wrap-map.api-0 :as w
     :refer [wrap empty-wrap vary with-wrap]]))
```

## 1. Default Values for Missing Keys

*Use Case*: You want a map that returns a specific default value (or `nil`) when a requested key is not found, instead of requiring the caller to provide a `nf` argument to `get`.

*How?*: Override `:valAt_k` and `:invoke-variadic`.

```clojure
(def default-value-map
  (-> empty-wrap
      (vary assoc
                 :valAt_k (fn [_ m k] (get m k :not-available))
                 :valAt_k_nf (fn [_ m k & [not-available]] (get m k (or not-available :not-available)))
                 :invoke-variadic (fn [_ m k & [not-available]]
                                    (get m k (or not-available :not-available))))))

(def m1 (assoc default-value-map :a 1))
```

### Example Usage:

```clojure
(get m1 :a)   ;=> 1
(m1 :a)       ;=> 1 (Arity-1 invoke defaults to get override)
(:a m1)       ;=> 1

(get m1 :b)   ;=> :not-available
(m1 :b)       ;=> :not-available
(:b m1)       ;=> :not-available

(get m1 :b :soon) ;=> :soon
(m1 :b :soon)     ;=> :soon
(:b m1 :soon)     ;=> :soon
```

## 2. Case-Insensitive String Keys

*Use Case*: You need a map where string keys are treated case-insensitively (e.g., for HTTP headers).

*How?*: Override key lookup and association to normalize string keys (e.g., to lowercase).

```clojure
(defn- normalize-key [k]
  (if (string? k) (.toLowerCase ^String k) k))

(def case-insensitive-map
  (-> {}
      (vary merge
                 {:valAt_k (fn [_ m k] (get m (normalize-key k)))
                  :valAt_k_nf (fn [_ m k nf] (get m (normalize-key k) nf))
                  :containsKey_k (fn [_ m k] (contains? m (normalize-key k)))
                  :assoc_k_v (fn [{:as e :keys [<-]} m k v]
                               (<- e (assoc m (normalize-key k) v)))
                  :without_k (fn [{:as e :keys [<-]} m k]
                               (<- e (dissoc m (normalize-key k))))})))

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

*How?*: Override `:assoc_k_v` to perform validation before associating.

```clojure
(require '[clojure.spec.alpha :as s])

(s/def ::name string?)
(s/def ::age pos-int?)

(def schema-map
  (-> empty-wrap
      (vary assoc
                 :assoc_k_v (fn [{:as e :keys [<-]} m k v]
                              (let [expected-type (case k :name ::name :age ::age :any)]
                                (if (or (= expected-type :any) (s/valid? expected-type v))
                                  (<- e (assoc m k v))
                                  (throw (ex-info "Schema validation failed"
                                                  {:key k :value v :expected (s/describe expected-type)}))))))))
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

*How?*: Override `:valAt_k` and `:valAt_k_nf` to log the access.

```clojure
(def access-log (atom []))

(def logging-read-map
  (-> {}
      (vary assoc
                 :valAt_k (fn [_ m k]
                            (swap! access-log conj [:get k])
                            (get m k))
                 :valAt_k_nf (fn [_ m k nf]
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

*How?*: Override `:assoc_k_v` and `:without_k`.

```clojure
(defn notify-change [change-type key value]
  (println "[Notification] Type:" change-type ", Key:" key ", Value:" value))

(def notifying-map
  (-> {}
      (vary assoc
                 :assoc_k_v (fn [{:as e :keys [<-]} m k v]
                              (notify-change :assoc k v)
                              (<- e (assoc m k v)))
                 :without_k (fn [{:as e :keys [<-]} m k]
                              (notify-change :dissoc k nil)
                              (<- e (dissoc m k))))))
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

*How?*: Override `:valAt_k` (and potentially `:valAt_k_nf`).

```clojure
(def computed-prop-map
  (-> (wrap :first-name "Jane" :last-name "Doe")
      (vary assoc
                 :valAt_k (fn [_ m k]
                            (if (= k :full-name)
                              (str (:first-name m) " " (:last-name m))
                              (get m k)))
                 :valAt_k_nf (fn [{:as e :keys [valAt_k]} m k nf]
                               (if (= k :full-name)
                                 (valAt_k e m k)   ;; <- Delegate to valAt_k
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

*How?*: Override `:valAt_k_nf`. If the key isn't present, attempt to load it. This example also updates the map to cache the loaded value.

```clojure
(defn simulate-db-fetch [k]
  (println "[DB] Fetching data for key:" k)
  (Thread/sleep 50) ; Simulate delay
  (if (= k :user-prefs) {:theme "dark" :lang "en"} nil))

(def lazy-loading-map
  (-> {}
      (vary assoc
                 :valAt_k_nf (fn [_ m k nf]
                               (let [v (get m k ::nf)]
                                 (if (= v ::nf)
                                   (if-let [loaded-val (simulate-db-fetch k)]
                                     (do
                                       (println "[Cache] Storing loaded value for key:" k)
                                       loaded-val) ;; Simple version: just return loaded, no cache update
                                     nf)
                                   v)))
                 :valAt_k (fn [{:as e :keys [valAt_k_nf]} m k]
                            (valAt_k_nf e m k ::nf))))) ; Delegate to above
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

## 8. Read-Only Map View

*Use Case*: Provide a map interface to data that should not be modified through that interface.

*How?*: Override all mutating impls (`assoc`, `dissoc`, `conj`, etc.) to throw `UnsupportedOperationException`.

```clojure
(defn read-only-error [& _]
  (throw (UnsupportedOperationException. "Wrap map is read-only")))

(def read-only-map-impls
  {:assoc_k_v read-only-error
   :without_k read-only-error
   :assocEx_k_v read-only-error
   ;; Override transient mutations too if you want `(transient read-only-map)` to fail
   :T_assoc_k_v read-only-error
   :T_without_k read-only-error
   :T_conj_v read-only-error})

(def read-only-m
  (-> (wrap :a 1)
      (with-wrap read-only-map-impls)))
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

*How?*: Override `:invoke-variadic`.

```clojure
(defn handle-add [x y] (+ x y))
(defn handle-multiply [x y] (* x y))

(def dispatching-map
  (-> {:add-fn handle-add :mul-fn handle-multiply}
      (w/assoc
        :invoke-variadic
        (fn [_ m operation & args]
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

*How?*: Override `:valAt_k` and `:valAt_k_nf`. Store counts in an atom external to the map.

```clojure
(def access-counts (atom {}))

(def counting-map
  (-> (wrap :a 1 :b 2)
      (w/assoc
        :valAt_k
        (fn [_ m k]
          (swap! access-counts update k (fnil inc 0)) ; Increment count
          (get m k))
        :valAt_k_nf
        (fn [_ m k nf]
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

*How?*: Override transient impls like `:T_assoc_k_v`.

```clojure
(def transiently-validating-map
  (-> empty-wrap
      (vary assoc
                 :T_assoc_k_v (fn [_ t-m k v]
                                (if (number? v)
                                  (assoc! t-m k v)
                                  (throw (ex-info "Transient validation failed: Value must be number" {:key k :value v})))))))
```

### Example Usage:

```clojure
;; Successful batch update
(persistent!
 (-> transiently-validating-map
     transient
     (assoc! :x 10)
     (assoc! :y 20)))
;=> {:x 10, :y 20}

(try
  (persistent!
   (-> transiently-validating-map
       transient
       (assoc! :x 10)
       (assoc! :y "not a number"))) ; This will throw
  (catch Exception e (ex-data e)))
;=> {:key :y, :value "not a number"}
```

## 12. Custom String Representation

*Use Case*: Control how the map is printed or converted to a string, perhaps hiding sensitive data or providing a summary.

*How?*: Override `:print-method_writer` and `:toString`.

```clojure
(def sanitizing-string-map
  (-> (wrap :user "secret-user" :id 123 :data [1 2 3])
      (w/assoc
        :print-method_writer
        (fn [_ m w]
          (doto w
            (.write "<SecureMapData id=")
            (.write (str (:id m)))
            (.write ">")))
        :toString
        (fn [_ m]
          (str "<SecureMapData id=" (:id m) ">")))))
```

### Example Usage:

```clojure
(str sanitizing-string-map)
;=> "<SecureMapData id=123>"

(println sanitizing-string-map)
; <SecureMapData id=123>
```
