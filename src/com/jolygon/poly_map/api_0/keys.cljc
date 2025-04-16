(ns com.jolygon.poly-map.api-0.keys
  "This ns is mostly just for documentation and for providing an alias.
  
  Defines standard keyword keys used in the implementations ('impls') map
  of a persistent PolyMap to override default map behaviors.

  Each var defined here holds the actual namespaced keyword as its value
  (e.g., `get_k` evaluates to `::get_k` in this ns). The documentation for
  the override is attached as metadata to the var.

  Override functions associated with these keywords receive the following
  arguments:
  - this       The PolyMap instance itself.
  - m       The underlying persistent data collection.
  - impls      The persistent implementations map itself.
  - metadata   The persistent metadata map of the PolyMap instance.
  Followed by operation-specific arguments like k, v, f, init, etc.

  Override functions are generally expected to return the final result
  that the corresponding Clojure interface method would return (e.g., a new
  PolyMap instance for assoc/dissoc, a value for get, a boolean for containsKey?).
  When returning a new PolyMap instance, ensure it's properly constructed,
  often using `com.jolygon.poly-map.api-0/make-poly-map` or similar internal
  constructors if needed, preserving implementations and metadata appropriately."
  (:refer-clojure :exclude [count empty seq iterator get assoc dissoc meta reduce])
  (:require
    [clojure.core :as c]))

(defn ^:private default-map-invoke
  "Default IFn invoke behavior for PolyMap when no custom ::invoke-variadic
  is provided. Mimics standard map lookup behavior: (map key) looks up key (arity 1),
  (map key nf) provides default (arity 2). Throws exceptions for all
  other arities (0, 3+)."
  ;; Arity 0: Invalid for map lookup
  ([_this _m _impls _metadata]
   (throw (ex-info "Invalid arity: 0"
                   {:error :invalid-arity
                    :arity 0
                    :args []})))
  ;; Arity 1: Standard map lookup (key)
  ([_this m _impls _metadata k]
   (c/get m k)) ;; Use aliased c/get
  ;; Arity 2: Standard map lookup (key, nf)
  ([_this m _impls _metadata k nf]
   (c/get m k nf)) ;; Use aliased c/get
  ;; Arity 3: Invalid for map lookup
  ([_this _m _impls _metadata a1 a2 a3]
   (throw (ex-info "Invalid arity: 3"
                   {:error :invalid-arity
                    :arity 3
                    :args [a1 a2 a3]})))
  ;; Variadic Arity (5+): Invalid for map lookup
  ([_this _m _impls _metadata a1 a2 a3 a4 & rest-args]
   (let [;; Calculate the actual total arity
         arity (+ 4 (c/count rest-args))
         ;; Combine all arguments for the error map
         all-args (concat [a1 a2 a3 a4] rest-args)]
     (throw (ex-info (str "Invalid arity: " arity)
                     {:error :invalid-arity
                      :arity arity
                      :args all-args})))))

(defn handle-invoke
  "Core IFn invocation handler for PolyMap instances.
  Checks for ::invoke-variadic in impls and calls it if present with exact
  arity arguments (0-20). Otherwise delegates to default-map-invoke with
  exact arity arguments. Uses apply only for arity > 20."
  ([this env impls metadata]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata)
     (default-map-invoke this env impls metadata)))
  ([this env impls metadata a1]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1)
     (default-map-invoke this env impls metadata a1)))
  ([this env impls metadata a1 a2]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2)
     (default-map-invoke this env impls metadata a1 a2)))
  ([this env impls metadata a1 a2 a3]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3)
     (default-map-invoke this env impls metadata a1 a2 a3)))
  ([this env impls metadata a1 a2 a3 a4]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4)
     (default-map-invoke this env impls metadata a1 a2 a3 a4)))
  ([this env impls metadata a1 a2 a3 a4 a5]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16)
     (default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16)))
  ([this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16 & rest-args]
   (if-let [poly-invoke (c/get impls ::invoke-variadic)]
     (apply poly-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16 rest-args)
     (apply default-map-invoke this env impls metadata a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16 rest-args))))

(defn construct
  "Internal function called when creating/updating PolyMap instances.
  Applies ::pm/construct hook if present.
  Returns a PolyMap."
  [this m impls metadata]
  (if-let [construct* (::construct impls)]
    (construct* this m impls metadata)
    [this m impls metadata]))

;; --- Protocols ---

(defprotocol IPolyAssociative
  "Protocol for managing the impls map ('m') of a persistent PolyMap."
  (-assoc-impl [coll k v] "Associates impl k with function v in the impls map. Returns new PolyMap.")
  (-contains-impl? [coll k] "Returns true if impl k exists in the impls map.")
  (-impl [coll k] "Returns the function associated with impl k, or nil.")
  (-get-impls [coll] "Returns the full persistent impls map.")
  (-set-impls [coll new-impls] "Replaces the entire impls map. Returns new PolyMap.")
  (-get-coll [coll] "Returns the underlying persistent data collection ('m').")
  (-dissoc-impl [coll k] "Removes impl k from the impls map. Returns new PolyMap."))

;; --- Core Map Operations ---

(def ^{:doc "Overrides `clojure.lang.ILookup/valAt` (get with 1 key argument).
            Expected fn signature: `(fn [this m impls metadata k] ...)`
            Should return: The value associated with `k`, or nil if not found."
       :arglists '([this m impls metadata k])}
  get_k ::get_k)

(def ^{:doc "Overrides `clojure.lang.ILookup/valAt` (get with key and nf arguments).
            Expected fn signature: `(fn [this m impls metadata k nf] ...)`
            Should return: The value associated with `k`, or the `nf` value."
       :arglists '([this m impls metadata k nf])}
  get_k_nf ::get_k_nf)

(def ^{:doc "Overrides `clojure.lang.Associative/assoc`.
            Expected fn signature: `(fn [this m impls metadata k v] ...)`
            Should return: The new persistent PolyMap instance with the key/value associated."
       :arglists '([this m impls metadata k v])}
  assoc_k_v ::assoc_k_v)

(def ^{:doc "Overrides `clojure.lang.IPersistentMap/without` (dissoc).
            Note: Core dissoc is variadic. This override receives only the first key `k`.
            Expected fn signature: `(fn [this m impls metadata k] ...)`
            Should return: The new persistent PolyMap instance with the key removed."
       :arglists '([this m impls metadata k])}
  without_k ::without_k)

(def ^{:doc "Overrides `clojure.lang.Associative/containsKey`.
            Expected fn signature: `(fn [this m impls metadata k] ...)`
            Should return: Boolean indicating if the key `k` is present."
       :arglists '([this m impls metadata k])}
  containsKey_k ::containsKey_k)

(def ^{:doc "Overrides `clojure.lang.Associative/entryAt`.
            Expected fn signature: `(fn [this m impls metadata k] ...)`
            Should return: A `clojure.lang.MapEntry` for the key `k`, or nil."
       :arglists '([this m impls metadata k])}
  entryAt_k ::entryAt_k)

(def ^{:doc "Overrides `clojure.lang.Counted/count`.
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: The integer count of items in the map."
       :arglists '([this m impls metadata])}
  count ::count)

(def ^{:doc "Overrides `clojure.lang.IPersistentCollection/empty`.
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: A new, empty PolyMap instance (typically keeping the same impls map and nil metadata)."
       :arglists '([this m impls metadata])}
  empty ::empty)

(def ^{:doc "Overrides `clojure.lang.IPersistentCollection/cons`.
            Expected fn signature: `(fn [this m impls metadata v] ...)` where `v` is the value to add (e.g., a MapEntry).
            Should return: The new persistent PolyMap instance with the item added."
       :arglists '([this m impls metadata v])}
  cons_v ::cons_v)

(def ^{:doc "Overrides `clojure.lang.IPersistentMap/assocEx`.
            Note: Maps typically treat assocEx like assoc (no exception on existing key).
            Expected fn signature: `(fn [this m impls metadata k v] ...)`
            Should return: The new persistent PolyMap instance with the key/value associated."
       :arglists '([this m impls metadata k v])}
  assocEx_k_v ::assocEx_k_v)

;; --- Iteration / Reduction ---

(def ^{:doc "Overrides `clojure.lang.Seqable/seq`.
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: A seq of the map's entries, or nil if empty."
       :arglists '([this m impls metadata])}
  seq ::seq)

(def ^{:doc "Overrides `java.lang.Iterable/iterator` (CLJ) or `IIterable.-iterator` (CLJS).
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: A relevant Iterator object over the map's entries."
       :arglists '([this m impls metadata])}
  iterator ::iterator)

(def ^{:doc "Overrides `clojure.lang.IMapIterable/keyIterator` (CLJ only?).
            Note: Less commonly overridden than seq/reduce.
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: A `java.util.Iterator` over the map's keys."
       :arglists '([this m impls metadata])}
  keyIterator ::keyIterator)

(def ^{:doc "Overrides `clojure.lang.IMapIterable/valIterator` (CLJ only?).
            Note: Less commonly overridden than seq/reduce.
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: A `java.util.Iterator` over the map's values."
       :arglists '([this m impls metadata])}
  valIterator ::valIterator)

(def ^{:doc "Overrides `clojure.core.protocols/coll-reduce` (1-arg arity: `(reduce f coll)`).
            Expected fn signature: `(fn [this m impls metadata f] ...)` where `f` is the reducing function.
            Should return: The final reduced value."
       :arglists '([this m impls metadata f])}
  coll-reduce_f ::coll-reduce_f)

(def ^{:doc "Overrides `clojure.core.protocols/coll-reduce` (2-arg arity: `(reduce f init coll)`).
            Expected fn signature: `(fn [this m impls metadata f init] ...)` where `f` is reducing fn, `init` is initial value.
            Should return: The final reduced value."
       :arglists '([this m impls metadata f init])}
  coll-reduce_f_init ::coll-reduce_f_init)

(def ^{:doc "Overrides `clojure.lang.IKVReduce/kvreduce` (CLJ) or `cljs.core.protocols/IKVReduce.-kv-reduce` (CLJS).
            Expected fn signature: `(fn [this m impls metadata f init] ...)` where `f` is `(fn [acc k v] ...)` reducing fn.
            Should return: The final reduced value."
       :arglists '([this m impls metadata f init])}
  kv-reduce_f_init ::kv-reduce_f_init)

;; --- Function Invocation ---

(def ^{:doc "Overrides `clojure.lang.IFn/invoke` and `applyTo`. Called when the PolyMap is invoked as a function.
            Note: The default implementation handles arities 1 & 2 as map lookups.
            This override replaces ALL function call behavior.
            Expected fn signature: `(fn [this m impls metadata & args] ...)`
            Should return: The result of the function call."
       :arglists '([this m impls metadata & args])}
  invoke-variadic ::invoke-variadic)

;; --- Metadata / Object / Equality ---

(def ^{:doc "Overrides `Object/toString`.
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: A string representation of the PolyMap."
       :arglists '([this m impls metadata])}
  toString ::toString)

(def ^{:doc "Overrides `clojure.lang.IHashEq/equiv` (CLJ) or `IEquiv.-equiv` (CLJS).
            Note: Default PolyMap equiv delegates to underlying value. Overriding this changes equality semantics. Be cautious.
            Expected fn signature: `(fn [this m impls metadata other] ...)` where `other` is the object to compare against.
            Should return: Boolean indicating equivalence."
       :arglists '([this m impls metadata other])}
  equiv_other ::equiv_other)

(def ^{:doc "Overrides `Object/hashCode` (CLJ) or `IHash.-hash` (CLJS).
            Note: Must be consistent with `equiv_other` if overridden. Default delegates to m.
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: An integer hash code."
       :arglists '([this m impls metadata])}
  hashCode ::hashCode)

(def ^{:doc "Overrides `clojure.lang.IObj/meta` (CLJ) or `IMeta.-meta` (CLJS).
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: The metadata map associated with the PolyMap."
       :arglists '([this m impls metadata])}
  meta ::meta)

(def ^{:doc "Overrides `clojure.lang.IObj/withMeta` (CLJ) or `IWithMeta.-with-meta` (CLJS).
            Expected fn signature: `(fn [this m impls metadata new-meta] ...)` where `new-meta` is the new metadata map.
            Should return: A new persistent PolyMap instance with the new metadata applied."
       :arglists '([this m impls metadata new-meta])}
  withMeta_meta ::withMeta_meta)

;; --- Transients ---

(def ^{:doc "Overrides `clojure.lang.IEditableCollection/asTransient` (CLJ) or `IEditableCollection.-as-transient` (CLJS).
            Expected fn signature: `(fn [this m impls metadata] ...)`
            Should return: A new `TransientPolyMap` instance."
       :arglists '([this m impls metadata])}
  asTransient ::asTransient)

;; --- Printing ---

(def ^{:doc "Overrides the `print-method` multimethod (CLJ) or `IPrintWithWriter.-pr-writer` protocol method (CLJS).
            Expected fn signature: `(fn [this m impls metadata writer] ...)` where `writer` is the output writer/stream. CLJS also receives `opts`.
            Should return: `nil` (CLJ) or the writer (CLJS). The function should perform writes to the writer directly."
       :arglists '([this m impls metadata writer])}
  print-method_writer ::print-method_writer)

;; --- java.util.Map Methods (CLJ Only) ---
;; These are less relevant for CLJS but kept here for CLJ compatibility info

(def ^{:doc "(CLJ only) Overrides `java.util.Map/size`. See `count`."}
  size ::size)
(def ^{:doc "(CLJ only) Overrides `java.util.Map/isEmpty`. See `count` or `seq`."}
  isEmpty ::isEmpty)
(def ^{:doc "(CLJ only) Overrides `java.util.Map/containsValue`. Often inefficient."}
  containsValue_v ::containsValue_v)
(def ^{:doc "(CLJ only) Overrides `java.util.Map/entrySet`."}
  entrySet ::entrySet)
(def ^{:doc "(CLJ only) Overrides `java.util.Map/keySet`."}
  keySet ::keySet)
(def ^{:doc "(CLJ only) Overrides `java.util.Map/values`."}
  values ::values)
(def ^{:doc "(CLJ only) Overrides `java.util.Map/put`. Unsupported on immutable maps."}
  put_k_v ::put_k_v)
(def ^{:doc "(CLJ only) Overrides `java.util.Map/remove`. Unsupported on immutable maps."}
  remove_k ::remove_k)
(def ^{:doc "(CLJ only) Overrides `java.util.Map/putAll`. Unsupported on immutable maps."}
  putAll_map ::putAll_map) ;; Note: Renamed var
(def ^{:doc "(CLJ only) Overrides `java.util.Map/clear`. Unsupported on immutable maps."}
  clear ::clear)
