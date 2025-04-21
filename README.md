# `wrap` map

_"map type maps"_

`wrap` maps provide a flexible way to create specialized map-like data structures in Clojure and ClojureScript. It allows you to intercept and customize standard map operations like `get`, `assoc`, `dissoc`, function invocation, printing, and more. This enables built-in validation, side effects, lazy loading, default values, case-insensitive keys, and other custom behaviors without needing to reimplement all the underlying map interfaces.

Think of it as adding middleware or aspects directly to your map data structure. It offers two ways to customize behavior:

1.  **High-Level API:** The casual and easy way. Uses simple keywords (e.g., `:get`, `:assoc`) to attach handlers for common map operations. Easier to use for most scenarios in applications or application specific data wrangling.
2.  **Low-Level API:** Provides fine-grained control by allowing overrides for specific underlying protocol/interface methods using namespaced keywords (e.g., `:valAt_k_nf`, `:T_assoc_k_v`). Useful for advanced cases or overriding methods not exposed by the high-level API. Prefer the low-level API when building libraries on top of `wrap` maps. Low level API versions are guaranteed remain stable.

## Motivation

Sometimes, you need a map that does *more* than just associate keys with values. You might want to:

* Validate data against a schema as it's being `assoc`'d.
* Provide computed default values when a key is missing (`get`).
* Trigger side effects (logging, notifications, DB persistence) when the map is modified.
* Log access patterns for debugging or analytics.
* Treat string keys case-insensitively.
* Implement lazy loading for specific keys.
* Make the map itself callable (`IFn`) to perform a specific action based on its content.
* Create read-only views of map data (using the low-level API).

`wrap` maps provide a structured and composable way to achieve these behaviors by wrapping a standard Clojure(Script) map and delegating operations through customizable handlers.

## Features

* **Behavioral Customization:** Override standard map operations via high-level keywords (`:get`, `:assoc`, `:dissoc`, etc.) or low-level method keys.
* **Function Call Override:** Make map instances callable with custom logic using the `:invoke` high-level keyword or low-level `:invoke`.
* **Custom Printing:** Control how the map is represented as a string using the `:print` high-level keyword or low-level keys.
* **Transient Support:** Efficient batch updates using transients, with support for overriding transient-specific operations via low-level keys.
* **Metadata Preservation:** Correctly handles metadata (`meta`, `with-meta`).
* **Clojure & ClojureScript:** Works consistently across both platforms.

## Compatibility

Developed and tested with Clojure 1.12.x and ClojureScript 1.11.x.

## Installation

Add the following dependency:

**deps.edn:**

```clojure
com.jolygon/wrap-map {:mvn/version "0.1.9"}
```

## Basic Usage (High-Level API)

Require the main API namespace, aliased as `w`.

```clojure
(require '[com.jolygon.wrap-map :as w :refer [wrap]])
```

You create a `wrap` map just like a regular map:

```clojure
(def m1 (wrap :a 1 :b 2))
;=> {:a 1, :b 2}

;; It behaves like a standard Clojure(Script) map by default:
(get m1 :a)       ;=> 1
(get m1 :c 404)   ;=> 404
(:b m1)           ;=> 2
(count m1)        ;=> 2
(assoc m1 :c 3)   ;=> {:a 1, :b 2, :c 3}
(dissoc m1 :a)    ;=> {:b 2}
(keys m1)         ;=> (:a :b)
(vals m1)         ;=> (1 2)

;; It's persistent:
(def m2 (assoc m1 :c 3))
m1 ;=> {:a 1, :b 2}
m2 ;=> {:a 1, :b 2, :c 3}

;; Transient support works as expected:
(persistent! (assoc! (transient m1) :d 4))
;=> {:a 1, :b 2, :d 4}
```

### Customizing Behavior (High-Level API):

Use `w/assoc` to attach behavior handlers using simple keywords. The first argument is a `wrap` map (or just a map), followed by keyword/handler pairs.

```clojure
(def default-value-map
  (-> (wrap :c 3)
      (w/assoc :get (fn [m k & [nf]]
                      (get m k (or nf :not-available))))))

(def m-with-default (assoc default-value-map :a 1))

(get m-with-default :a) ;=> 1
(get m-with-default :b) ;=> :not-available
(get m-with-default :b :explicit-nf) ;=> :explicit-nf (uses provided not-found)
(m-with-default :b) ;=> :not-available (:invoke behavior defaults to :get)

;; Example 2: Case-Insensitive String Keys
(defn- normalize-key [k]
  (if (string? k) (.toLowerCase ^String k) k))

(def case-insensitive-map
  (-> {:other :keys :in :a :regular :map}
      (w/assoc ;<- `w/assoc` and friends auto-`wrap` their map arg when needed
       :assoc     (fn [m k v]      (assoc m (normalize-key k) v))
       :dissoc    (fn [m k]        (dissoc m (normalize-key k)))
       :contains? (fn [m k]        (contains? m (normalize-key k)))
       :get       (fn [m k & [nf]] (get m (normalize-key k) nf)))))

(def headers (-> case-insensitive-map (assoc "Content-Type" "application/json")))

(get headers "content-type") ;=> "application/json"
(contains? headers "CONTENT-TYPE") ;=> true
(dissoc headers "Content-type") ;=> {:other :keys :in :a :regular :map}
```

## Core Concept: High-Level Behaviors

The high-level `w/assoc` function associates handler functions with specific behavior keywords. These keywords generally correspond to common map operations.

### Available Behavior Keywords:

* `:get`: Overrides key lookup (`get`, keyword invocation, map-as-function arity-1/arity-2).

    * Handler signature: `(fn [m k] ...)` or `(fn [m k nf] ...)`

* `:assoc`: Overrides key/value association (`clojure.core/assoc`).

    * Handler signature: `(fn [m k v] ...)`
    * Must return: The new underlying map after association.

* `:dissoc`: Overrides key removal (`clojure.core/dissoc`).

    * Handler signature: `(fn [m k] ...)`
    * _Must return:_ The new underlying map after dissociation.

* `:contains?`: Overrides key presence check (contains?).

    * Handler signature: `(fn [m k] ...)`
    * _Must return:_ Boolean.

* `:invoke`: Overrides map-as-function behavior for all arities.

    * Handler signature: `(fn [m & args] ...)`

* `:print`: Overrides how the map is printed (`print-method`, `str`).

    * Handler signature: `(fn [m] ...)`
    * _Must return:_ A string representation.

When you use `w/assoc`, it translates the behavior keyword (e.g., `:get`) into one or more low-level implementation keys (e.g., `:valAt_k`, `:valAt_k_nf`) and registers your handler function appropriately using the low-level `assoc-impl` mechanism. However, if a high level key is not available, `w/assoc` behaves just like `assoc-impl`, so you can use `w/assoc` for both.

## Advanced Usage (Low-Level API)

For finer control, direct access to underlying protocol/interface methods, or to implement behaviors not covered by the high-level keywords (like complex transient interactions or read-only maps), you can use the low-level API.

1. **Structure**: A `WrapMap` internally holds:
    - `e`: A persistent map where keys are reserved, specific **unqualified keywords** and values are functions that override default implementation for the method associated with the keyword.
    - `m`: The underlying map holding the actual data.

2. **Implementation Keys**: Override functions are associated with namespace _unqualified_ keyword keys.
    - For persistent map operations in Clojurescript:
        > :toString :-conj_v :-empty :-without_k :-assoc_k_v :-contains-key?_k :-find_k :-seq :-meta :withMeta_new-meta :-count :-lookup_k :-lookup_k_nf :kv-reduce_f_init  :invoke :invoke-variadic :-pr-writer_writer_opts
    - For transient map operations in Clojurescript:
        > :T_-conj! :T_-assoc!_k_v :T_-dissoc!_k :T_-lookup_k :T_-lookup_k_nf :T_-count
    - For persistent map operations in Clojure:
        > :toString :containsKey_k :entryAt_k :assoc_k_v :kvreduce_f_init :valAt_k :valAt_k_nf :keyIterator :valIterator :count :empty :cons_v :assocEx_k_v :without_k :seq :iterator :invoke :invoke-variadic :asTransient :withMeta_meta :meta :coll-reduce_afn :coll-reduce_afn_init :kv-reduce_afn_init :size :isEmpty :containsValue_v :get_k :get_k_nf :entrySet :keySet :values :put :remove :putAll :clear :print-method_writer
    - For transient map operations in Clojure:
        > :T_conj_v :T_assoc_k_v :T_without_k :T_valAt_k :T_valAt_k_nf :T_count 

3. **Override Function Signatures**: Low-level override functions receive more arguments. They often need to return a variant of `WrapMap`using the `<-` constructor function - in the form of: `(<- e m)`. `TransientWrapMap`, on the other hand, handle's returning it's own `this` on mutating operations - all you have to do is perform the mutating operations on the transient map (`t_m`) and it will be returned. You cannot make changes to the implementations environment map `e` while in transient mode. You are only provided `e` for informational access to the implementations and metadata, for meta programming purposes.

4. **Providing Low-Level Implementations**: Use `w/vary` or `w/assoc`. `w/assoc` can handle both high level and low level keys.

```clojure
;; Example: Read-Only Map (Requires Low-Level API)
(defn read-only-error [& _]
  (throw (UnsupportedOperationException. "Map is read-only")))

(def read-only-map-impls
 {:assoc_k_v   read-only-error ;; Override persistent assoc
  :without_k   read-only-error ;; Override persistent dissoc
  :cons_v      read-only-error ;; Override persistent conj
  :assocEx_k_v read-only-error
  ;; Override transient mutations too
  :T_assoc_k_v read-only-error
  :T_without_k read-only-error
  :T_conj_v    read-only-error})

(def read-only-m
  (-> (wrap :a 1)
      (w/vary merge read-only-map-impls)))

;; Usage
(get read-only-m :a) ;=> 1
(try (assoc read-only-m :b 2) (catch Exception e (.getMessage e)))
;=> "Map is read-only"
(try (persistent! (assoc! (transient read-only-m) :c 3)) (catch Exception e (.getMessage e)))
;=> "Map is read-only"

;; Example 2 - surgical modifications (here logging) in a functional pipeline

(-> {:a 1}
    (assoc :b 2)
    (w/assoc
      :T_assoc_k_v (fn [_ t-m k v]
                     (println "[Transient] assoc! key:" k "val:" v)
                     (assoc! t-m k v)))
    transient
    (assoc! :x 100)
    (assoc! :y 200)
    persistent!
    w/unwrap
    (dissoc :b)
    (w/assoc
      :assoc_k_v (fn [{:as e :keys [<-]} m k v]
                   (println "[Persistent] assoc key:" k "val:" v)
                   (<- e (assoc m k v)))) ;<- persistent ops require `<- constructor
    (assoc :z 300)
    w/unwrap
    (assoc :done 1))
; [Transient] assoc! key: :x val: 100
; [Transient] assoc! key: :y val: 200
; [Persistent] assoc key: :z val: 300
{:a 1, :x 100, :y 200, :z 300, :done 1}
```

### Examples

For more detailed examples covering both APIs, see:

- [High level examples](./bench/ex/examples-high-level.md) (using `w/assoc` with keywords)
- [Low level examples](./bench/ex/examples.md) (using `w/vary`, etc.)

### Performance

Significant performance optimizations have been implemented, including specializing internal types and optimizing constructors.

* **Overall**: Based on recent benchmarks (Run 5/6), baseline `wrap` map operations (reads, writes, construction, reduction, batch transient updates) now perform very close to, and sometimes exceed, the speed of standard Clojure/Script hash maps and transients.
* **CLJ**: The geometric mean across baseline operations showed `wrap` maps at ~95% the speed of standard maps.
* **CLJS**: The geometric mean across baseline operations showed `wrap` maps at ~72% the speed of standard maps, heavily influenced by the `persistent!` cost. Many individual CLJS operations (writes, reductions) were faster than standard maps.
* **Bottleneck**: The primary remaining bottleneck relative to standard maps appears to be the cost of transitioning from a transient `wrap` back to a persistent one (`persistent!`), especially in ClojureScript.
* **Overrides**: Adding custom behavior via handlers still incurs some overhead compared to baseline `wrap` map operations, which is expected. However, the baseline is now much faster.

See [./bench/ex/clj-bench.md](./bench/ex/clj-bench.md) for Clojure benchmark details and [./bench/ex/cljs-bench.md](./bench/ex/cljs-bench.md) for ClojureScript benchmark details. Contributions for further optimization are welcome!

### See Also

* **Potemkin** (`def-map-type`): Potemkin's `def-map-type` is excellent for creating _new, specific map-like types_ that efficiently implement map interfaces, often based on delegating to underlying fields or structures. Choose `def-map-type` when you need a new, static, record-like data type with map semantics. Choose `wrap` maps when you want to add dynamic behaviors (validation, logging, computation, interception) to existing map data or general-purpose map structures without defining a whole new type, or when you want to change behaviors dynamically using `assoc-impl`/`vary`.
* `defrecord` / `deftype`: Suitable for creating fixed-schema, efficient data structures. They can implement protocols for map-like behavior, but you implement the methods directly. Less flexible for dynamic behavior modification compared to `wrap` maps.
* **Protocols**: Clojure's protocols allow defining interfaces that different types can implement. You could define a protocol for custom map behavior, but `wrap` maps provide a ready-made implementation structure focused specifically on wrapping and intercepting standard map operations.
* **Schema Libraries (Malli, Spec)**: Primarily focused on data validation and specification, often used externally to map operations rather than being baked into the map's behavior itself, although they can be integrated using `wrap` handlers (as shown in examples).
* **Proxy**: Allows dynamic implementation of interfaces, but generally comes with a larger performance overhead than `deftype` or `wrap` map's approach.

### Development

Clone the repository and run tests using the Clojure CLI:

```bash
# Clojure tests
clj -X:test-clj

# ClojureScript tests (requires NodeJS)
clj -M:test-cljs
```

To run benchmarks:

### Run Clojure benchmarks
```bash
clj -M:benchmark-clj
```

### Run ClojureScript benchmarks
```bash
clj -M:benchmark-cljs-node
```

### License

Copyright © 2025 Jolygon

Distributed under the MIT license. See LICENSE file for details.
