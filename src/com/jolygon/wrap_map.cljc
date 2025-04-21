(ns com.jolygon.wrap-map
  "Public API for creating and manipulating wrap map instances.
  
  Provides functions to:
  - Create WrapMaps (`wrap`, `empty-wrap`).
  - Manage implementation overrides (`vary`).
  - Add behaviors (`assoc`).
  - Return the underlying persistent hash map (`unwrap`).
  - Freeze maps to prevent further implementation changes (`freeze`)."
  (:refer-clojure :exclude [assoc])
  (:require
    [com.jolygon.wrap-map.api-0 :as w]))

(def empty-wrap
  "A pre-defined empty WrapMap instance with no overrides."
  (w/make-wrap))

(def ^{:doc "Creates a new wrap map instance containing the supplied key-value pairs.

  Accepts either:
  - A single argument `m` which is an existing map to wrap.
  - A variadic list of key-value arguments `k1 v1 k2 v2 ...`.

  If any keys in the key-value list are equal, they are handled as if by
  repeated uses of `clojure.core/assoc` on the underlying map."
       :arglists '([& kvs])}
  wrap w/wrap)

(def ^{:doc "Returns a 'frozen' version of the wrap map `coll`.

  A frozen wrap map prevents further modification of its implementation
  overrides environment via functions like `w/assoc`, `assoc-impl`,
  or `vary`. Attempts to modify the implementations of a frozen map
  will throw an exception."
       :arglists '([coll])}
  freeze w/freeze)

(def ^{:doc "Applies function `afn` to the current implementation overrides
  environment of the WrapMap `coll`, passing `args` as additional arguments
  to `afn`.

  `afn` should take the current Impl environment map as its first argument
  and return the new environment map to be used.

  If `coll` is not already a wrap map, it will be implicitly wrapped using
  `(wrap coll)` before applying `afn`.

  Returns a new wrap map variant with the implementations resulting from `afn`."
       :arglists '([coll afn & args])}
  vary w/vary)

(def ^{:doc "Returns the underlying persistent data collection being wrapped."
       :arglists '([coll])}
  unwrap w/unwrap)

;; High Level API

(def ^{:doc "Associates behavior overrides on a map `coll`.

  Takes the map `coll` followed by key-value pairs where the key is a
  behavior keyword (e.g., :get, :assoc, :dissoc, :contains?, :invoke, :print)
  and the value is the corresponding handler function.

  Args:
    behavior key A keyword identifying the behavior to override
                 (:get, :assoc, :dissoc, :contains?, :invoke, :print, or
                  a raw implementation key).
    handler fn   The function to handle the specified behavior. Its expected
                 signature depends on `behavior-key`:
                 - :get: `(fn [underlying-map k] ...)` or
                         `(fn [underlying-map k not-found] ...)` - the wrapper
                           will try to call the matching arity.
                 - :assoc: `(fn [underlying-map k v] new-underlying-map)`
                 - :dissoc: `(fn [underlying-map k] new-underlying-map)`
                 - :contains?: `(fn [underlying-map k] boolean)`
                 - :invoke: `(fn [underlying-map & args] ...)`
                 - :print: `(fn [underlying-map] string-representation)`
                 - Raw key: Depends on the specific low-level key contract.

  Example:
  (assoc my-wrap-map
         :get (fn [m k] (str \"Got: \" (clojure.core/get m k)))
         :assoc (fn [m k v] (clojure.core/assoc m (keyword k) (str v))))

  Returns a new wrap map variant with the specified behaviors associated."
       :arglists '([coll & {:as e}])}
  assoc w/assoc)
