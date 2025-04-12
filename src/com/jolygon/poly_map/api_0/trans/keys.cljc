(ns com.jolygon.poly-map.api-0.trans.keys
  "This ns is mostly just for documentation and for providing an alias.
  
  Defines standard keyword keys used in the implementations ('impls') map of a
  TransientPolyMap to override default transient map behaviors.
  
  Each var defined here holds the actual namespaced keyword as its value
  (e.g., `assoc_k_v` evaluates to `::assoc_k_v` in this ns).
  The documentation for the override is attached as metadata to the var.
  
  Example expected signature for `::assoc_k_v`:
  `(fn [this t_m impls metadata k v] ...)`
  
  Overrides for mutating operations (`assoc!`, `conj!`, `dissoc!`)
  should generally return the `this` (the TransientPolyMap instance) to allow chaining.
  The override for `persistent!` should return the new persistent `PolyMap` with all
  internal maps persisted as well.
  Overrides for read operations should return the requested value."
  (:refer-clojure :exclude [count persistent meta]))

(def ^{:doc "Overrides `ITransientAssociative.-assoc!` (assoc!).
            Expected fn signature: `(fn [this t_m impls metadata k v] ...)`
            Should return: The updated `this` TransientPolyMap instance."
       :arglists '([this t_m impls metadata k v])}
  assoc_k_v ::assoc_k_v)

(def ^{:doc "Overrides `ITransientMap.-dissoc!` (dissoc!).
            Expected fn signature: `(fn [this t_m impls metadata k] ...)`
            Should return: The updated `this` TransientPolyMap instance."
       :arglists '([this t_m impls metadata k])}
  without_k ::without_k)

(def ^{:doc "Overrides `ITransientCollection.-conj!` (conj!).
            Expected fn signature: `(fn [this t_m impls metadata entry] ...)`
            Should return: The updated `this` TransientPolyMap instance."
       :arglists '([this t_m impls metadata entry])}
  conj!_entry ::conj_entry)

(def ^{:doc "Overrides `ITransientCollection.-persistent!`.
            Expected fn signature: `(fn [this t_m impls metadata] ...)`
            Should return: The new persistent `PolyMap` instance."
       :arglists '([this t_m impls metadata])}
  persistent ::persistent)

;; --- Read/Query Operations (Return Value) ---

(def ^{:doc "Overrides `ILookup.-lookup` (get with 1 key argument) on transient.
            Expected fn signature: `(fn [this t_m impls metadata k] ...)`
            Should return: The value associated with `k`, or nil if not found."
       ;; Note: Var name corresponds to `valAt_k` from CLJ for consistency.
       :arglists '([this t_m impls metadata k])}
  valAt_k ::valAt_k)

(def ^{:doc "Overrides `ILookup.-lookup` (get with key and nf) on transient.
            Expected fn signature: `(fn [this t_m impls metadata k nf] ...)`
            Should return: The value associated with `k`, or the `nf` value."
       ;; Note: Var name corresponds to `valAt_k_nf` from CLJ.
       :arglists '([this t_m impls metadata k nf])}
  valAt_k_nf ::valAt_k_nf)

(def ^{:doc "Overrides `ICounted.-count` on transient.
            Expected fn signature: `(fn [this t_m impls metadata] ...)`
            Should return: The integer count of items in the transient map."
       :arglists '([this t_m impls metadata])}
  count ::count)

;; --- Metadata ---

(def ^{:doc "Overrides `IMeta.-meta` on transient.
            Expected fn signature: `(fn [this t_m impls metadata] ...)`
            Should return: The metadata map associated with the transient map."
       :arglists '([this t_m impls metadata])}
  meta ::meta)

(def ^{:doc "Overrides `IWithMeta.-with-meta` on transient.
            Note: `-with-meta` on transients returns a *persistent* collection in CLJS.
            Expected fn signature: `(fn [this t_m impls metadata new-meta] ...)`
            Should return: A new persistent `PolyMap` instance with the metadata applied."
       :arglists '([this t_m impls metadata new-meta])}
  withMeta_meta ::withMeta_meta)