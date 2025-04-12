(ns com.jolygon.poly-map.api-0.impl
  "Internal implementation details for PolyMap.
  Provides the PolyMap and TransientPolyMap deftypes and related protocols.
  Users should generally prefer the API functions in 'com.jolygon.poly-map.api-0'
  or subsequent API versions, and use the impl keys defined in
  'com.jolygon.poly-map.api-0.keys' and 'com.jolygon.poly-map.trans.keys'.
  This namespace is subject to change."
  (:require
   [com.jolygon.poly-map.api-0.keys :as pm]
   [com.jolygon.poly-map.api-0.trans.keys :as tpm])
  (:import
   [clojure.lang Counted IEditableCollection IFn IHashEq IKVReduce ILookup
    IMapIterable IObj IPersistentCollection IPersistentMap Associative IMeta
    ITransientAssociative ITransientCollection ITransientMap MapEntry
    MapEquivalence Seqable]
   [java.util.concurrent.atomic AtomicBoolean]
   [java.util Map Set Collection]
   [java.lang Iterable UnsupportedOperationException IllegalAccessError Object]
   [java.io Writer]))

#_(set! *warn-on-reflection* true)

;;----------------------------------------------------------------------
;; Transient Implementation
;;----------------------------------------------------------------------

(declare poly-map*)

(defn- ^:private ensure-editable
  "Throws an exception if the transient's editable flag is false."
  [^AtomicBoolean editable?]
  (when-not (.get editable?)
    (throw (IllegalAccessError. "Transient used after persistent! call"))))

(deftype TransientPolyMap [^AtomicBoolean editable?
                           ^ITransientMap t_m
                           ^IPersistentMap impls
                           ^IPersistentMap metadata]
  ITransientMap
  ITransientCollection
  (conj [this entry]
    (ensure-editable editable?)
    (if-let [f (::tpm/conj_entry impls)]
      (f this t_m impls metadata entry)
      (do (.conj ^ITransientCollection t_m entry)
          ^TransientPolyMap this)))
  (persistent [this]
    (ensure-editable editable?)
    (.set editable? false)
    (if-let [f (::tpm/persistent impls)]
      (f this t_m impls metadata)
      (let [[_ ^IPersistentMap pe ^IPersistentMap nm ^IPersistentMap m]
            (pm/construct this ^IPersistentMap (.persistent ^ITransientCollection t_m) impls metadata)]
        (poly-map* pe nm m))))
  ITransientAssociative
  (assoc [this k v]
    (ensure-editable editable?)
    (if-let [f (::tpm/assoc_k_v impls)]
      (f this t_m impls metadata k v)
      (do (.assoc ^ITransientAssociative t_m k v)
          ^TransientPolyMap this)))
  (without [this k]
    (ensure-editable editable?)
    (if-let [f (::tpm/without_k impls)]
      (f this t_m impls metadata k)
      (do (.without ^ITransientMap t_m k)
          ^TransientPolyMap this)))
  ILookup
  (valAt [this k]
    (ensure-editable editable?)
    (if-let [f (::tpm/valAt_k impls)]
      (f this t_m impls metadata k)
      (.valAt ^ILookup t_m k)))
  (valAt [this k nf]
    (ensure-editable editable?)
    (if-let [f (::tpm/valAt_k_nf impls)]
      (f this t_m impls metadata k nf)
      (.valAt ^ILookup t_m k nf)))
  Counted
  (count [this]
    (ensure-editable editable?)
    (if-let [f (::tpm/count impls)]
      (f this t_m impls metadata)
      ^int (.count ^Counted t_m))))

(defn transient-poly-map*
  "Internal raw constructor (CLJS). Prefer API functions."
  (^TransientPolyMap []
   (TransientPolyMap. true {} {} {}))
  (^TransientPolyMap [m]
   (TransientPolyMap. true m {} {}))
  (^TransientPolyMap [m impls]
   (TransientPolyMap. true m impls {}))
  (^TransientPolyMap [m impls metadata]
   (TransientPolyMap. true m impls metadata))
  (^TransientPolyMap [edit m impls metadata]
   (TransientPolyMap. edit m impls metadata)))

;;----------------------------------------------------------------------
;; Persistent Implementation
;;----------------------------------------------------------------------

(deftype PolyMap [^IPersistentMap m
                  ^IPersistentMap impls
                  ^IPersistentMap metadata]
  IPersistentMap
  IHashEq
  MapEquivalence
  (hashCode [_this] (.hashCode ^Object m))
  (equiv [_this o]
    (if (instance? IPersistentMap o) (.equiv ^IPersistentMap m ^Object o) (.equals ^Object m ^Object o)))
  (hasheq [_this] (.hasheq ^IHashEq m))
  Object
  (equals [_this o] (.equals ^Object m ^Object o))
  (toString [this] (if-let [f (::pm/toString impls)] (f this m impls metadata) (.toString ^Object m)))
  pm/IPolyAssociative
  (-assoc-impl [_this k v] (PolyMap. m (.assoc ^IPersistentMap impls k v) metadata))
  (-contains-impl? [_this k] (contains? impls k))
  (-impl [_this k] (get ^ILookup impls k))
  (-get-impls [_this] impls)
  (-set-impls [_this new-impls] (PolyMap. m ^IPersistentMap new-impls metadata))
  (-get-coll [_this] m)
  (-dissoc-impl [_this k] (PolyMap. m (.without ^IPersistentMap impls k) metadata))
  Associative
  (containsKey [this k]
    (if-let [f (::pm/containsKey_k impls)]
      (f this m impls metadata k)
      ^boolean (.containsKey ^Associative m k)))
  (entryAt [this k]
    (if-let [f (::pm/entryAt_k impls)]
      (f this m impls metadata k)
      (.entryAt ^Associative m k)))
  (assoc [this k v]
    (if-let [f (::pm/assoc_k_v impls)]
      (f this m impls metadata k v)
      (let [[_ ^IPersistentMap pe ^IPersistentMap nm ^IPersistentMap m]
            (pm/construct this (.assoc ^IPersistentMap m k v) impls metadata)]
        (PolyMap. pe nm m))))
  IKVReduce
  (kvreduce [this f init]
    (if-let [f (::pm/kvreduce_f_init impls)]
      (f this m impls metadata f init)
      (reduce-kv f init m)))
  ILookup
  (valAt [this k]
    (if-let [f (::pm/valAt_k impls)]
      (f this m impls metadata k)
      (.valAt ^ILookup m k)))
  (valAt [this  k nf]
    (if-let [f (::pm/valAt_k_nf impls)]
      (f this m impls metadata k nf)
      (.valAt ^ILookup m k nf)))
  IMapIterable
  (keyIterator [this]
    (if-let [f (::pm/keyIterator impls)]
      (f this m impls metadata)
      (.keyIterator ^IMapIterable m)))
  (valIterator [this]
    (if-let [f (::pm/valIterator impls)]
      (f this m impls metadata)
      (.valIterator ^IMapIterable m)))
  Counted
  (^int count [this]
    (if-let [f (::pm/count impls)]
      (f this m impls metadata)
      (.count ^Counted m)))
  IPersistentCollection
  (^IPersistentCollection empty [this]
    (if-let [f (::pm/empty impls)]
      (f this m impls metadata)
      (PolyMap. (.empty ^IPersistentCollection m) impls metadata)))
  (^IPersistentCollection cons [this v]
    (if-let [f (::pm/cons_v impls)]
      (f this m impls metadata v)
      (let [[_ ^IPersistentMap pe ^IPersistentMap nm ^IPersistentMap m]
            (pm/construct this (.cons ^IPersistentCollection m v) impls metadata)]
        (PolyMap. pe nm m))))
  (^IPersistentMap assocEx [this k v]
    (if-let [f (::pm/assocEx_k_v impls)]
      (f this m impls metadata k v)
      (let [[_ ^IPersistentMap pe ^IPersistentMap nm ^IPersistentMap m]
            (pm/construct this (.assocEx ^IPersistentMap m k v) impls metadata)]
        (PolyMap. pe nm m))))
  (^IPersistentMap without [this k]
    (if-let [f (::pm/without_k impls)]
      (f this m impls metadata k)
      (let [[_ ^IPersistentMap pe ^IPersistentMap nm ^IPersistentMap m]
            (pm/construct this (.without ^IPersistentMap m k) impls metadata)]
        (PolyMap. pe nm m))))
  Seqable
  (seq [this]
    (if-let [f (::pm/seq impls)]
      (f this m impls metadata)
      (.seq ^Seqable m)))
  Iterable
  (iterator [this]
    (if-let [f (::pm/iterator impls)]
      (f this m impls metadata)
      (.iterator
       ^Iterable
       m)))
  IFn
  (invoke [this] (pm/handle-invoke this m impls metadata))
  (invoke [this k] (pm/handle-invoke this m impls metadata k))
  (invoke [this k nf] (pm/handle-invoke this m impls metadata k nf))
  (invoke [this arg1 arg2 arg3] (pm/handle-invoke this m impls metadata arg1 arg2 arg3))
  (invoke [this arg1 arg2 arg3 arg4] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4))
  (invoke [this arg1 arg2 arg3 arg4 arg5] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20] (pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20))
  (invoke [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args] (apply pm/handle-invoke this m impls metadata arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args))
  (applyTo [this args] (apply pm/handle-invoke this m impls metadata args))
  IEditableCollection
  (^ITransientCollection asTransient [this]
    (if-let [f (::pm/asTransient impls)]
      (f this m impls metadata)
      (TransientPolyMap. (AtomicBoolean. true) (transient m) impls metadata)))
  IObj
  (withMeta [this new-meta]
    (if-let [f (::pm/withMeta_meta impls)]
      (f this m impls metadata new-meta)
      (PolyMap. m impls new-meta)))
  IMeta
  (meta [this]
    (if-let [f (::pm/meta impls)]
      (f this m impls metadata)
      metadata))
  clojure.core.protocols.CollReduce
  (coll-reduce [this afn]
    (if-let [f (::pm/coll-reduce_afn impls)]
      (f this m impls metadata afn)
      (reduce afn m)))
  (coll-reduce [this afn init]
    (if-let [f (::pm/coll-reduce_afn_init impls)]
      (f this m impls metadata afn init)
      (reduce afn init m)))
  clojure.core.protocols.IKVReduce
  (kv-reduce [this afn init]
    (if-let [f (::pm/kv-reduce_afn_init impls)]
      (f this m impls metadata afn init)
      (reduce-kv afn init m)))
  Map
  (^int size [this]
    (if-let [f (::pm/size impls)]
      (f this m impls metadata)
      (.count ^Counted m)))
  (^boolean isEmpty [this]
    (if-let [f (::pm/isEmpty impls)]
      (f this m impls metadata)
      (zero? (.count ^Counted this))))
  (^boolean containsValue [this v]
    (if-let [f (::pm/containsValue_v impls)]
      (f this m impls metadata v)
      (boolean (some #(= ^Object v %) (vals m)))))
  (^Object get [this k]
    (if-let [f (::pm/get_k impls)]
      (f this m impls metadata k)
      (.valAt ^ILookup m k)))
  (^Set entrySet [this]
    (if-let [f (::pm/entrySet impls)]
      (f this m impls metadata)
      (set (.seq ^Seqable this))))
  (^Set keySet [this]
    (if-let [f (::pm/keySet impls)]
      (f this m impls metadata)
      (set (keys m))))
  (^Collection values [this]
    (if-let [f (::pm/values impls)]
      (f this m impls metadata)
      (vals m)))
  (put [this k v]
    (if-let [f (::pm/put_k_v impls)]
      (f this m impls metadata k v)
      (throw (UnsupportedOperationException. "put not supported on immutable PolyMap"))))
  (remove [this k]
    (if-let [f (::pm/remove_k impls)]
      (f this m impls metadata k)
      (throw (UnsupportedOperationException. "remove not supported on immutable PolyMap"))))
  (putAll [this mx]
    (if-let [f (::pm/putAll impls)]
      (f this m impls metadata mx)
      (throw (UnsupportedOperationException. "putAll not supported on immutable PolyMap"))))
  (clear [this]
    (if-let [f (::pm/get impls)]
      (f this m impls metadata)
      (throw (UnsupportedOperationException. "clear not supported on immutable PolyMap")))))

;;----------------------------------------------------------------------
;; Printing
;;----------------------------------------------------------------------

(defmethod print-method PolyMap [^PolyMap pm ^Writer w]
  (let [^IPersistentMap m        (.m pm)
        ^IPersistentMap impls    (.impls pm)
        ^IPersistentMap metadata (.metadata pm)]
    (if-let [f (::pm/print-method_writer impls)]
      (f pm m impls metadata w)
      (do
        (.write w "{")
        (loop [first? true, xs (seq m)]
          (when xs
            (when-not first? (.write w ", "))
            (let [^MapEntry entry (first xs)]
              (print-method (key entry) w)
              (.write w " ")
              (print-method (val entry) w))
            (recur false (next xs))))
        (.write w "}")))))

;;----------------------------------------------------------------------
;; Constructor
;;----------------------------------------------------------------------

(defn poly-map*
  "Internal raw constructor for PolyMap. Creates a PolyMap instance
  directly from the underlying collection `m` and impls map
  `impls`. Does NOT perform the internal preparation step (like
  ensuring default-invoke). Prefer `com.jolygon.poly-map.api-0/poly-map`
  for general use."
  ^PolyMap
  ([] (PolyMap. {} {} {}))
  ^PolyMap
  ([^IPersistentMap m] (PolyMap. m {} {}))
  ^PolyMap
  ([^IPersistentMap m ^IPersistentMap impls] (PolyMap. m impls {}))
  ^PolyMap
  ([^IPersistentMap m ^IPersistentMap impls ^IPersistentMap metadata]
   (PolyMap. m impls metadata)))
