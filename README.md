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
* **Function Call Override:** Make map instances callable with custom logic using the `:invoke` high-level keyword or low-level `::pm/invoke-variadic`.
* **Custom Printing:** Control how the map is represented as a string using the `:print` high-level keyword or low-level keys.
* **Transient Support:** Efficient batch updates using transients, with support for overriding transient-specific operations via low-level keys (`::tpm/...`).
* **Metadata Preservation:** Correctly handles metadata (`meta`, `with-meta`).
* **Extensible:** Add arbitrary helper functions or data to the low-level impls map alongside overrides.
* **Clojure & ClojureScript:** Works consistently across both platforms.

## Compatibility

Developed and tested with Clojure 1.11.x and ClojureScript 1.11.x.

## Installation

Add the following dependency:

**deps.edn:**
```clojure
com.jolygon/poly-map {:mvn/version "x.x.x"} ; Replace x.x.x with the latest version
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
    - `com.jolygon.poly-map.api-0.keys`: For persistent map operations (e.g., `::pm/valAt_k_nf`). Recommended alias: `pm`.
    - `com.jolygon.poly-map.api-0.trans.keys`: For transient map operations (e.g., `::tpm/assoc_k_v`). Recommended alias: `tpm`.

```clojure
(require '[com.jolygon.poly-map.api-0 :as poly] ;; Use poly for high-level too
         '[com.jolygon.poly-map.api-0.keys :as pm]
         '[com.jolygon.poly-map.api-0.trans.keys :as tpm])

pm/valAt_k_nf ;;=> :com.jolygon.poly-map.api-0.keys/valAt_k_nf
tpm/assoc_k_v  ;;=> :com.jolygon.poly-map.api-0.trans.keys/assoc_k_v
```

Refer to the docstrings of the vars in these namespaces for a full list of available keys and the exact interface methods they correspond to.

3. **Override Function Signatures**: Low-level override functions receive more arguments:

    - **Persistent**: `(fn [this m impls metadata & operation-args] ...)`
    - **Transient**: `(fn [this t_m impls metadata & operation-args] ...)`
        - `this`: The PolyMap or TransientPolyMap instance.
        - `m`/`t_m`: The underlying persistent/transient data map.
        - `impls`: The implementations map.
        - `metadata`: The metadata map.
        - `operation-args`: Arguments specific to the low-level operation (e.g., `k`, `v`, `nf`, `f`, `init`).
    - **Return Values**: Must return the exact type expected by the underlying protocol/interface method (e.g., a new persistent `PolyMap` for `::pm/assoc_k_v`, the transient this for `::tpm/assoc_k_v`). Often requires using `poly/make-poly-map` or `poly/make-transient-poly-map` for construction. See `impl` key docstrings and source (`impl.clj`) for details.

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

## API Overview

The public API is in `com.jolygon.poly-map.api-0`. Future versions may use `.api-N`.

**Key Functions**:

- **High-Level API**:
    - `poly-map`, `empty-poly-map`: Create persistent poly-map instances.
    - `assoc`: Attaches behavior handlers using keywords (`:get`, `:assoc`, etc.). Returns a new persistent poly-map.
    - `dissoc`: Removes behavior handlers associated with specific keywords. Returns a new persistent poly-map.
- **Low-Level API**:
    - `assoc-impl`, `dissoc-impl`: Add/remove low-level impl overrides using namespaced keywords (returns new persistent map).
    - `set-impls`: Replace the entire low-level impls map (returns new persistent map).
    - `get-impls`: Get the persistent low-level impls map.
    - `get-coll`: Get the underlying persistent data map (`m`).
    - `contains-impl?`, `get-impl`: Check for/retrieve a specific low-level override function.
    - `make-poly-map`, `make-transient-poly-map`: Raw constructors (use with caution, prefer standard creation/update functions).

- **Implementation Key Definitions**:

    - `com.jolygon.poly-map.api-0.keys`: Vars holding keywords for **persistent** low-level overrides (`::pm/...`).
    - `com.jolygon.poly-map.api-0.trans.keys`: Vars holding keywords for **transient** low-level overrides (`::tpm/...`).

Please refer to the docstrings of the API functions and the impl key vars for detailed usage and signatures.

### Examples

See the high-level and low-level API code snippets provided earlier in this README for common use cases.

For more detailed examples covering both APIs, see:

- [examples-high-level.md](./examples-high-level.md) (using `poly/assoc` with keywords)
- [examples.md](./examples.md) (using `poly/assoc-impl` with `::pm/...` keys)

_(Suggestion: Consider adding the high-level examples file or merging relevant examples into `examples.md`)_

### Performance

- **General**: Expect poly-maps to be roughly 25% to 50% as fast as native hash-maps for mixed workloads currently.
- **Reads**: Simple read operations (`get`, keyword lookup) can sometimes meet or slightly exceed native hash-map performance due to direct delegation.
- **Writes**/**Updates**: Operations requiring the construction of new poly-map instances (`assoc`, `dissoc`) incur more overhead.
- **Known Bottlenecks**:
    - Calling `apply` on a poly-map instance can be particularly slow.
    - Repeatedly `assoc`ing onto very large poly-maps needs optimization.
- **Note**: Performance hasn't been a primary focus yet. The main overhead often comes from the custom logic added via handlers, which would exist elsewhere in your program anyway. Contributions for optimization are welcome!

See [bench/ex/clj-bench.md]() for benchmark details.

### Development

Clone the repository and run tests using the Clojure CLI:

```bash
# Clojure tests
clj -X:test-clj

# ClojureScript tests (requires NodeJS)
clj -Atest-cljs
```

To run benchmarks:

# Run Clojure benchmarks
```bash
clj -Alibra
```

### License

Copyright © 2025 Jolygon

Distributed under the MIT license. See LICENSE file for details.