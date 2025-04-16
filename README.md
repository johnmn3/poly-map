# poly-map

_"map type maps"_

`poly-map` provides a highly flexible way to create specialized map-like data structures in Clojure and ClojureScript. It allows you to intercept and customize standard map operations like `get`, `assoc`, `dissoc`, function invocation, printing, and more. This enables built-in validation, side effects, lazy loading, default values, case-insensitive keys, and other custom behaviors without needing to reimplement all the underlying map interfaces.

Think of it as adding middleware or aspects directly to your map data structure. It offers two ways to customize behavior:

1.  **High-Level API:** The casual and easy way. Uses simple keywords (e.g., `:get`, `:assoc`) to attach handlers for common map operations. Easier to use for most scenarios in applications or application specific data wrangling.
2.  **Low-Level API:** Provides fine-grained control by allowing overrides for specific underlying protocol/interface methods using namespaced keywords (e.g., `::pm/valAt_k_nf`, `::tpm/assoc_k_v`). Useful for advanced cases or overriding methods not exposed by the high-level API. Prefer the low-level API when building libraries on top of poly-maps. API versions are guaranteed remain stable.

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

`poly-map` provides a structured and composable way to achieve these behaviors by wrapping a standard Clojure(Script) map and delegating operations through customizable handlers.

## Features

* **Behavioral Customization:** Override standard map operations via high-level keywords (`:get`, `:assoc`, `:dissoc`, etc.) or low-level method keys.
* **Function Call Override:** Make map instances callable with custom logic using the `:invoke` high-level keyword or low-level `::pm/invoke`.
* **Custom Printing:** Control how the map is represented as a string using the `:print` high-level keyword or low-level keys.
* **Transient Support:** Efficient batch updates using transients, with support for overriding transient-specific operations via low-level keys (`::tpm/...`).
* **Metadata Preservation:** Correctly handles metadata (`meta`, `with-meta`).
* **Extensible:** Add arbitrary helper functions or data to the low-level impls map alongside overrides.
* **Clojure & ClojureScript:** Works consistently across both platforms.

## Compatibility

Developed and tested with Clojure 1.12.x and ClojureScript 1.12.x.

## Installation

Add the following dependency:

**deps.edn:**

```clojure
com.jolygon/poly-map {:mvn/version "0.1.3"}
```

## Basic Usage (High-Level API)

Require the main API namespace, aliased as `poly`.

```clojure
(require '[com.jolygon.poly-map.api-0 :as poly :refer [poly-map empty-poly-map]])
```

Create a poly-map just like a regular map:

```clojure
(def m1 (poly-map :a 1 :b 2))
;=> {:a 1, :b 2}

;; It behaves like a standard Clojure(Script) map by default:
(get m1 :a)       ;=> 1
(get m1 :c 404)   ;=> 404
(:b m1)           ;=> 2
(count m1)        ;=> 2
(assoc m1 :c 3)   ;=> {:a 1, :b 2, :c 3} ;; Note: Uses clojure.core/assoc
(dissoc m1 :a)    ;=> {:b 2} ;; Note: Uses clojure.core/dissoc
(keys m1)         ;=> (:a :b) ;; Order may vary in CLJS
(vals m1)         ;=> (1 2)  ;; Order may vary in CLJS

;; It's persistent:
(def m2 (assoc m1 :c 3))
m1 ;=> {:a 1, :b 2}
m2 ;=> {:a 1, :b 2, :c 3}

;; Transient support works as expected:
(persistent! (assoc! (transient m1) :d 4))
;=> {:a 1, :b 2, :d 4}
```

### Customizing Behavior (High-Level API):

Use `poly/assoc` to attach behavior handlers using simple keywords. The first argument is the poly-map, followed by keyword/handler pairs.

```clojure
(def default-value-map
  (-> empty-poly-map
      (poly/assoc :get (fn [m k & [nf]]
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
  (-> empty-poly-map
      (poly/assoc
       :assoc     (fn [m k v]      (assoc m (normalize-key k) v))
       :dissoc    (fn [m k]        (dissoc m (normalize-key k)))
       :contains? (fn [m k]        (contains? m (normalize-key k)))
       :get       (fn [m k & [nf]] (get m (normalize-key k) nf)))))

(def headers (-> case-insensitive-map (assoc "Content-Type" "application/json")))

(get headers "content-type") ;=> "application/json"
(contains? headers "CONTENT-TYPE") ;=> true
(dissoc headers "Content-type") ;=> {}
```

## Core Concept: High-Level Behaviors

The high-level `poly/assoc` function associates handler functions with specific behavior keywords. These keywords generally correspond to common map operations.

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

_(Note: More behaviors might be added. Refer to the `com.jolygon.poly-map.api-0/assoc` docstring for the definitive list.)_

When you use `poly/assoc`, it translates the behavior keyword (e.g., `:get`) into one or more low-level implementation keys (e.g., `::pm/valAt_k`, `::pm/valAt_k_nf`) and registers your handler function appropriately using the low-level `assoc-impl` mechanism.

## Advanced Usage (Low-Level API)

For finer control, direct access to underlying protocol/interface methods, or to implement behaviors not covered by the high-level keywords (like complex transient interactions or read-only maps), you can use the low-level API.

1. **Structure**: A `PolyMap` internally holds:
    - `m`: The underlying persistent map holding the actual data.
    - `impls`: A persistent map where keys are specific **namespaced keywords** and values are functions that override default behavior.
    - `metadata`: The map's metadata.

2. **Implementation Keys**: Override functions are associated with namespaced keyword keys defined in:
    - `com.jolygon.poly-map.api-0.keys`: For persistent map operations (e.g., `::pm/valAt_k_nf`).
    - `com.jolygon.poly-map.api-0.trans.keys`: For transient map operations (e.g., `::tpm/assoc_k_v`).

3. **Override Function Signatures**: Low-level override functions receive more arguments (see `keys.cljc` and `trans/keys.cljc` docstrings for details). They often need to return a new `PolyMap` instance (for persistent ops) or `this` (for transient ops).

4. **Providing Implementations**: Use `poly/assoc-impl`, `poly/dissoc-impl`, `poly/set-impls`.

```clojure
;; Example: Read-Only Map (Requires Low-Level API)
(defn read-only-error [& _]
  (throw (UnsupportedOperationException. "Map is read-only")))

(def read-only-map-impls
 {::pm/assoc_k_v    read-only-error ;; Override persistent assoc
  ::pm/without_k    read-only-error ;; Override persistent dissoc
  ::pm/cons_v       read-only-error ;; Override persistent conj
  ::pm/assocEx_k_v  read-only-error
  ;; Override transient mutations too
  ::tpm/assoc_k_v    read-only-error
  ::tpm/without_k    read-only-error
  ::tpm/conj_entry   read-only-error})

(def read-only-m
  (-> (poly-map :a 1)
      (poly/set-impls read-only-map-impls))) ;; Use set-impls to replace all impls

;; Usage
(get read-only-m :a) ;=> 1
(try (assoc read-only-m :b 2) (catch Exception e (.getMessage e)))
;=> "Map is read-only"
(try (persistent! (assoc! (transient read-only-m) :c 3)) (catch Exception e (.getMessage e)))
;=> "Map is read-only"
```

### Examples

For more detailed examples covering both APIs, see:

- [examples-high-level.md](./bench/ex/examples-high-level.md) (todo) (using `poly/assoc` with keywords)
- [examples.md](./bench/ex/examples.md) (using `poly/assoc-impl` with `::pm/...` keys)

### Performance

Significant performance optimizations have been implemented, including specializing internal types and optimizing constructors.

* **Overall**: Based on recent benchmarks (Run 5/6), baseline `poly-map` operations (reads, writes, construction, reduction, batch transient updates) now perform very close to, and sometimes exceed, the speed of standard Clojure/Script hash maps and transients.
* **CLJ**: The geometric mean across baseline operations showed `poly-map` at ~95% the speed of standard maps.
* **CLJS**: The geometric mean across baseline operations showed `poly-map` at ~72% the speed of standard maps, heavily influenced by the `persistent!` cost. Many individual CLJS operations (writes, reductions) were faster than standard maps.
* **Bottleneck**: The primary remaining bottleneck relative to standard maps appears to be the cost of transitioning from a transient poly-map back to a persistent one (`persistent!`), especially in ClojureScript.
* **Overrides**: Adding custom behavior via handlers still incurs some overhead compared to baseline poly-map operations, which is expected. However, the baseline is now much faster.

See ./bench/ex/clj-bench.md for Clojure benchmark details and ./bench/ex/cljs-bench.md for ClojureScript benchmark details. Contributions for further optimization are welcome!

### See Also

* **Potemkin** (`def-map-type`): Potemkin's `def-map-type` is excellent for creating _new, specific map-like types_ that efficiently implement map interfaces, often based on delegating to underlying fields or structures. Choose `def-map-type` when you need a new, static, record-like data type with map semantics. Choose `poly-map` when you want to add dynamic behaviors (validation, logging, computation, interception) to existing map data or general-purpose map structures without defining a whole new type, or when you want to change behaviors dynamically using `assoc-impl`/`set-impls`.
* `defrecord` / `deftype`: Suitable for creating fixed-schema, efficient data structures. They can implement protocols for map-like behavior, but you implement the methods directly. Less flexible for dynamic behavior modification compared to `poly-map`.
* **Protocols**: Clojure's protocols allow defining interfaces that different types can implement. You could define a protocol for custom map behavior, but `poly-map` provides a ready-made implementation structure focused specifically on wrapping and intercepting standard map operations.
* **Schema Libraries (Malli, Spec)**: Primarily focused on data validation and specification, often used externally to map operations rather than being baked into the map's behavior itself, although they can be integrated using `poly-map` handlers (as shown in examples).
* **Proxy**: Allows dynamic implementation of interfaces, but generally comes with a larger performance overhead than `deftype` or `poly-map`'s approach.

### Changelog

#### v0.1.0 (YYYY-MM-DD)

* Major Performance Optimizations:
  * Implemented specialized internal types (PolyMap+...) to significantly speed up baseline assoc and get operations by reducing runtime dispatch overhead.
  * Optimized poly-map constructor, especially when called via apply, bringing performance close to native hash-map.
  * Improved transient batch assoc! performance to be nearly on par with native transients.
  * Improved persistent! performance, though it remains an area with overhead compared to native maps.
* Introduced High-Level API: Added poly/assoc and poly/dissoc functions using simple keywords (e.g., :get, :assoc) for easier customization of common behaviors.
* Added examples-high-level.md (TODO) and updated documentation.

### Development

Clone the repository and run tests using the Clojure CLI:

```bash
# Clojure tests
clj -X:test-clj

# ClojureScript tests (requires NodeJS)
clj -M:test-cljs
```

To run benchmarks:

# Run Clojure benchmarks
```bash
clj -M:libra
```

### License

Copyright © 2025 Jolygon

Distributed under the MIT license. See LICENSE file for details.
