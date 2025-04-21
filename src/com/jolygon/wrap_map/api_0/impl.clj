(ns com.jolygon.wrap-map.api-0.impl
  "Internal implementation details for WrapMap.
  Provides the WrapMap and TransientWrapMap deftypes and related protocols.
  Users should generally prefer the API functions in 'com.jolygon.wrap-map.api-0'
  or subsequent API versions, and use the impl keys defined in
  'com.jolygon.wrap-map.api-0.common' and 'com.jolygon.wrap-map.trans.common'.
  This namespace is subject to change."
  (:require
    [com.jolygon.wrap-map.api-0.common :as wc])
  (:import
    [clojure.lang Counted IEditableCollection IFn IHashEq IKVReduce ILookup
     IMapIterable IObj IPersistentCollection IPersistentMap Associative IMeta
     ITransientAssociative ITransientCollection ITransientMap MapEntry
     MapEquivalence Seqable]
    [java.io Writer]
    [java.lang Iterable UnsupportedOperationException Object]
    [java.util Map Set Collection]))

#_(set! *warn-on-reflection* true)

(def allowable-impls
  #{:frozen? :metadata :this :twrap :pwrap :<-
    :T_conj_v :T_persistent :T_assoc_k_v :T_without_k :T_valAt_k :T_valAt_k_nf
    :T_count :toString :containsKey_k :entryAt_k :assoc_k_v :kvreduce_f_init :valAt_k
    :valAt_k_nf :keyIterator :valIterator :count :empty :cons_v :assocEx_k_v :without_k
    :seq :iterator :invoke :invoke-variadic :asTransient :withMeta_meta :meta :coll-reduce_afn
    :coll-reduce_afn_init :kv-reduce_afn_init :size :isEmpty :containsValue_v :get_k
    :get_k_nf :entrySet :keySet :values :put :remove :putAll :clear :print-method_writer})

#_{:clj-kondo/ignore [:shadowed-var]}
(defrecord Impls
           [frozen? metadata this twrap pwrap <-
            T_conj_v T_persistent T_assoc_k_v T_without_k T_valAt_k T_valAt_k_nf
            T_count toString containsKey_k entryAt_k assoc_k_v kvreduce_f_init valAt_k
            valAt_k_nf keyIterator valIterator count empty cons_v assocEx_k_v without_k
            seq iterator invoke invoke-variadic asTransient withMeta_meta meta coll-reduce_afn
            coll-reduce_afn_init kv-reduce_afn_init size isEmpty containsValue_v get_k
            entrySet keySet values put remove putAll clear print-method_writer])

(def empty-impls (map->Impls {}))

;;----------------------------------------------------------------------
;; Transient Implementation
;;----------------------------------------------------------------------

(declare ->WrapMap ->WrapMap+assoc_k_v ->WrapMap+valAt_k ->WrapMap+assoc_k_v|valAt_k construct
         ->TransientWrapMap ->TransientWrapMap+assoc_k_v ->TransientWrapMap+valAt_k
         ->TransientWrapMap+assoc_k_v|valAt_k)

(deftype TransientWrapMap
         [^Impls e
          ^:unsynchronized-mutable ^ITransientMap t_m]

  ITransientMap
  ITransientCollection
  (conj [this entry]
    (if-let [f (.-T_conj_v e)]
      (set! t_m (f e t_m entry))
      (set! t_m (.conj ^ITransientCollection t_m entry)))
    this)
  (persistent [_this]
    ((.-pwrap e) e ^IPersistentMap (persistent! t_m)))
  ITransientAssociative
  (assoc [this k v]
    (set! t_m (.assoc ^ITransientAssociative t_m k v))
    this)
  (without [this k]
    (if-let [f (.-T_without_k e)]
      (set! t_m (f e t_m k))
      (set! t_m (.without ^ITransientMap t_m k)))
    this)
  ILookup
  (valAt [_this k]
    (.valAt ^ILookup t_m k))
  (valAt [_this k nf]
    (.valAt ^ILookup t_m k nf))
  Counted
  (count [_this]
    (if-let [f (.-T_count e)]
      (f e t_m)
      ^int (.count ^Counted t_m))))

(deftype TransientWrapMap+assoc_k_v
         [^Impls e
          ^:unsynchronized-mutable ^ITransientMap t_m]
  ITransientMap
  ITransientCollection
  (conj [this entry]
    (if-let [f (.-T_conj_v e)]
      (set! t_m (f e t_m entry))
      (set! t_m (.conj ^ITransientCollection t_m entry)))
    this)
  (persistent [_this]
    ((.-pwrap e) e ^IPersistentMap (persistent! t_m)))
  ITransientAssociative
  (assoc [this k v]
    (set! t_m ((.-T_assoc_k_v e) e t_m k v))
    this)
  (without [this k]
    (if-let [f (.-T_without_k e)]
      (set! t_m (f e t_m k))
      (set! t_m (.without ^ITransientMap t_m k)))
    this)
  ILookup
  (valAt [_this k]
    (.valAt ^ILookup t_m k))
  (valAt [_this k nf]
    (.valAt ^ILookup t_m k nf))
  Counted
  (count [_this]
    (if-let [f (.-T_count e)]
      (f e t_m)
      ^int (.count ^Counted t_m))))

(deftype TransientWrapMap+valAt_k
         [^Impls e
          ^:unsynchronized-mutable ^ITransientMap t_m]
  ITransientMap
  ITransientCollection
  (conj [this entry]
    (if-let [f (.-T_conj_v e)]
      (set! t_m (f e t_m entry))
      (set! t_m (.conj ^ITransientCollection t_m entry)))
    this)
  (persistent [_this]
    ((.-pwrap e) e ^IPersistentMap (persistent! t_m)))
  ITransientAssociative
  (assoc [this k v]
    (set! t_m (.assoc ^ITransientAssociative t_m k v))
    this)
  (without [this k]
    (if-let [f (.-T_without_k e)]
      (set! t_m (f e t_m k))
      (set! t_m (.without ^ITransientMap t_m k)))
    this)
  ILookup
  (valAt [_this k]
    ((.-T_valAt_k e) e t_m k))
  (valAt [_this k nf]
    ((.-T_valAt_k_nf e) e t_m k nf))
  Counted
  (count [_this]
    (if-let [f (.-T_count e)]
      (f e t_m)
      ^int (.count ^Counted t_m))))

(deftype TransientWrapMap+assoc_k_v|valAt_k
         [^Impls e
          ^:unsynchronized-mutable ^ITransientMap t_m]
  ITransientMap
  ITransientCollection
  (conj [this entry]
    (if-let [f (.-T_conj_v e)]
      (set! t_m (f e t_m entry))
      (set! t_m (.conj ^ITransientCollection t_m entry)))
    this)
  (persistent [_this]
    ((.-pwrap e) e ^IPersistentMap (persistent! t_m)))
  ITransientAssociative
  (assoc [this k v]
    (set! t_m ((.-T_assoc_k_v e) e t_m k v))
    this)
  (without [this k]
    (if-let [f (.-T_without_k e)]
      (set! t_m (f e t_m k))
      (set! t_m (.without ^ITransientMap t_m k)))
    this)
  ILookup
  (valAt [_this k]
    ((.-T_valAt_k e) e t_m k))
  (valAt [_this k nf]
    ((.-T_valAt_k_nf e) e t_m k nf))
  Counted
  (count [_this]
    (if-let [f (.-T_count e)]
      (f e t_m)
      ^int (.count ^Counted t_m))))

;;----------------------------------------------------------------------
;; Persistent Implementation
;;----------------------------------------------------------------------

(deftype WrapMap [^Impls e ^IPersistentMap m]
  IPersistentMap
  IHashEq
  MapEquivalence
  (hashCode [_this] (.hashCode ^Object m))
  (equiv [_this o]
    (if (instance? IPersistentMap o) (.equiv ^IPersistentMap m ^Object o) (.equals ^Object m ^Object o)))
  (hasheq [_this] (.hasheq ^IHashEq m))
  Object
  (equals [_this o] (.equals ^Object m ^Object o))
  (toString [_this] (if-let [f (.-toString e)] (f e m) (.toString ^Object m)))
  wc/IWrapAssociative
  (-assoc-impl [_this k v]
    (assert (allowable-impls k))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls (assoc e k v)) m)))
  (-contains-impl? [_this k] (get e k))
  (-impl [_this k] (get ^ILookup e k))
  (-get-impls [_this] e)
  (-with-wrap [_this new-impls]
    (assert (every? allowable-impls (keys new-impls)))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls new-impls) m)))
  (-vary [_this afn args]
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (let [new-impls (apply afn e args)]
        (assert (every? allowable-impls (keys new-impls)))
        (construct (map->Impls new-impls) m))))
  (-unwrap [_this] m)
  (-dissoc-impl [_this k]
    (assert (allowable-impls k))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls (assoc ^Impls e k nil)) m)))
  (-freeze [_this]
    (WrapMap. (assoc e :frozen? true) m))
  Associative
  (containsKey [_this k]
    (if-let [f (.-containsKey_k e)]
      (f e m k)
      ^boolean (.containsKey ^Associative m k)))
  (entryAt [_this k]
    (if-let [f (.-entryAt_k e)]
      (f e m k)
      (.entryAt ^Associative m k)))

  (assoc [_this k v]
    (WrapMap. e ^IPersistentMap (.assoc ^IPersistentMap m k v)))

  IKVReduce
  (kvreduce [_this f init]
    (if-let [f (.-kvreduce_f_init e)]
      (f e m f init)
      (reduce-kv f init m)))
  ILookup

  (valAt [_this k] (.valAt ^ILookup m k))
  (valAt [_this k nf] (.valAt ^ILookup m k nf))

  IMapIterable
  (keyIterator [_this]
    (if-let [f (.-keyIterator e)]
      (f e m)
      (.keyIterator ^IMapIterable m)))
  (valIterator [_this]
    (if-let [f (.-valIterator e)]
      (f e m)
      (.valIterator ^IMapIterable m)))
  Counted
  (^int count [_this]
    (if-let [f (.-count e)]
      (f e m)
      (.count ^Counted m)))
  IPersistentCollection
  (^IPersistentCollection empty [_this]
    (if-let [f (.-empty e)]
      (f e m)
      (WrapMap. e (.empty ^IPersistentCollection m))))
  (^IPersistentCollection cons [_this v]
    (if-let [f (.-cons_v e)]
      (f e m v)
      (WrapMap. e ^IPersistentMap (.cons ^IPersistentCollection m v))))
  (^IPersistentMap assocEx [_this k v]
    (if-let [f (.-assocEx_k_v e)]
      (f e m k v)
      (WrapMap. e ^IPersistentMap (.assocEx ^IPersistentMap m k v))))
  (^IPersistentMap without [_this k]
    (if-let [f (.-without_k e)]
      (f e m k)
      (WrapMap. e ^IPersistentMap (.without ^IPersistentMap m k))))
  Seqable
  (seq [_this]
    (if-let [f (.-seq e)]
      (f e m)
      (.seq ^Seqable m)))
  Iterable
  (iterator [_this]
    (if-let [f (.-iterator e)]
      (f e m)
      (.iterator ^Iterable m)))
  IFn
  (invoke [_this] (wc/handle-invoke e m))
  (invoke [_this k] (wc/handle-invoke e m k))
  (invoke [_this k nf] (wc/handle-invoke e m k nf))
  (invoke [_this arg1 arg2 arg3] (wc/handle-invoke e m arg1 arg2 arg3))
  (invoke [_this arg1 arg2 arg3 arg4] (wc/handle-invoke e m arg1 arg2 arg3 arg4))
  (invoke [_this arg1 arg2 arg3 arg4 arg5] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args] (apply wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args))
  (applyTo [_this args] (apply wc/handle-invoke e m args))
  IEditableCollection
  (^ITransientCollection asTransient [_this]
    ((.-twrap e) e (transient m)))
  IObj
  (withMeta [_this new-meta]
    (if-let [f (.-withMeta_meta e)]
      (f e m new-meta)
      (WrapMap. (assoc e :metadata new-meta) m)))
  IMeta
  (meta [_this]
    (if-let [f (.-meta e)]
      (f e m)
      (:metadata e)))
  clojure.core.protocols.CollReduce
  (coll-reduce [_this afn]
    (if-let [f (.-coll-reduce_afn e)]
      (f e m afn)
      (reduce afn m)))
  (coll-reduce [_this afn init]
    (if-let [f (.-coll-reduce_afn_init e)]
      (f e m afn init)
      (reduce afn init m)))
  clojure.core.protocols.IKVReduce
  (kv-reduce [_this afn init]
    (if-let [f (.-kv-reduce_afn_init e)]
      (f e m afn init)
      (reduce-kv afn init m)))
  Map
  (^int size [_this]
    (if-let [f (.-size e)]
      (f e m)
      (.count ^Counted m)))
  (^boolean isEmpty [_this]
    (if-let [f (.-isEmpty e)]
      (f e m)
      (zero? (.count ^Counted m))))
  (^boolean containsValue [_this v]
    (if-let [f (.-containsValue_v e)]
      (f e m v)
      (boolean (some #(= ^Object v %) (vals m)))))
  (^Object get [_this k]
    (if-let [f (.-get_k e)]
      (f e m k)
      (.valAt ^ILookup m k)))
  (^Set entrySet [_this]
    (if-let [f (.-entrySet e)]
      (f e m)
      (set (.seq ^Seqable m))))
  (^Set keySet [_this]
    (if-let [f (.-keySet e)]
      (f e m)
      (set (keys m))))
  (^Collection values [_this]
    (if-let [f (.-values e)]
      (f e m)
      (vals m)))
  (put [_this k v]
    (if-let [f (.-put_k_v e)]
      (f e m k v)
      (throw (UnsupportedOperationException. "put not supported on immutable WrapMap"))))
  (remove [_this k]
    (if-let [f (.-remove_k e)]
      (f e m k)
      (throw (UnsupportedOperationException. "remove not supported on immutable WrapMap"))))
  (putAll [_this mx]
    (if-let [f (.-putAll e)]
      (f e m mx)
      (throw (UnsupportedOperationException. "putAll not supported on immutable WrapMap"))))
  (clear [_this]
    (if-let [f (.-get e)]
      (f e m)
      (throw (UnsupportedOperationException. "clear not supported on immutable WrapMap")))))

(deftype WrapMap+assoc_k_v [^Impls e ^IPersistentMap m]
  IPersistentMap
  IHashEq
  MapEquivalence
  (hashCode [_this] (.hashCode ^Object m))
  (equiv [_this o]
    (if (instance? IPersistentMap o) (.equiv ^IPersistentMap m ^Object o) (.equals ^Object m ^Object o)))
  (hasheq [_this] (.hasheq ^IHashEq m))
  Object
  (equals [_this o] (.equals ^Object m ^Object o))
  (toString [_this] (if-let [f (.-toString e)] (f e m) (.toString ^Object m)))
  wc/IWrapAssociative
  (-assoc-impl [_this k v]
    (assert (allowable-impls k))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls (assoc e k v)) m)))
  (-contains-impl? [_this k] (get e k))
  (-impl [_this k] (get ^ILookup e k))
  (-get-impls [_this] e)
  (-with-wrap [_this new-impls]
    (assert (every? allowable-impls (keys new-impls)))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls new-impls) m)))
  (-vary [_this afn args]
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (let [new-impls (apply afn e args)]
        (assert (every? allowable-impls (keys new-impls)))
        (construct (map->Impls new-impls) m))))
  (-unwrap [_this] m)
  (-dissoc-impl [_this k]
    (assert (allowable-impls k))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls (assoc ^Impls e k nil)) m)))
  (-freeze [_this]
    (WrapMap+assoc_k_v. (assoc e :frozen? true) m))
  Associative
  (containsKey [_this k]
    (if-let [f (.-containsKey_k e)]
      (f e m k)
      ^boolean (.containsKey ^Associative m k)))
  (entryAt [_this k]
    (if-let [f (.-entryAt_k e)]
      (f e m k)
      (.entryAt ^Associative m k)))

  (assoc [_this k v] ((.-assoc_k_v e) e m k v))

  IKVReduce
  (kvreduce [_this f init]
    (if-let [f (.-kvreduce_f_init e)]
      (f e m f init)
      (reduce-kv f init m)))
  ILookup

  (valAt [_this k] (.valAt ^ILookup m k))
  (valAt [_this k nf] (.valAt ^ILookup m k nf))

  IMapIterable
  (keyIterator [_this]
    (if-let [f (.-keyIterator e)]
      (f e m)
      (.keyIterator ^IMapIterable m)))
  (valIterator [_this]
    (if-let [f (.-valIterator e)]
      (f e m)
      (.valIterator ^IMapIterable m)))
  Counted
  (^int count [_this]
    (if-let [f (.-count e)]
      (f e m)
      (.count ^Counted m)))
  IPersistentCollection
  (^IPersistentCollection empty [_this]
    (if-let [f (.-empty e)]
      (f e m)
      (WrapMap+assoc_k_v. e (.empty ^IPersistentCollection m))))
  (^IPersistentCollection cons [_this v]
    (if-let [f (.-cons_v e)]
      (f e m v)
      (WrapMap+assoc_k_v. e ^IPersistentMap (.cons ^IPersistentCollection m v))))
  (^IPersistentMap assocEx [_this k v]
    (if-let [f (.-assocEx_k_v e)]
      (f e m k v)
      (WrapMap+assoc_k_v. e ^IPersistentMap (.assocEx ^IPersistentMap m k v))))
  (^IPersistentMap without [_this k]
    (if-let [f (.-without_k e)]
      (f e m k)
      (WrapMap+assoc_k_v. e ^IPersistentMap (.without ^IPersistentMap m k))))
  Seqable
  (seq [_this]
    (if-let [f (.-seq e)]
      (f e m)
      (.seq ^Seqable m)))
  Iterable
  (iterator [_this]
    (if-let [f (.-iterator e)]
      (f e m)
      (.iterator ^Iterable m)))
  IFn
  (invoke [_this] (wc/handle-invoke e m))
  (invoke [_this k] (wc/handle-invoke e m k))
  (invoke [_this k nf] (wc/handle-invoke e m k nf))
  (invoke [_this arg1 arg2 arg3] (wc/handle-invoke e m arg1 arg2 arg3))
  (invoke [_this arg1 arg2 arg3 arg4] (wc/handle-invoke e m arg1 arg2 arg3 arg4))
  (invoke [_this arg1 arg2 arg3 arg4 arg5] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args] (apply wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args))
  (applyTo [_this args] (apply wc/handle-invoke e m args))
  IEditableCollection
  (^ITransientCollection asTransient [_this]
    ((.-twrap e) e (transient m)))
  IObj
  (withMeta [_this new-meta]
    (if-let [f (.-withMeta_meta e)]
      (f e m new-meta)
      (WrapMap+assoc_k_v. (assoc e :metadata new-meta) m)))
  IMeta
  (meta [_this]
    (if-let [f (.-meta e)]
      (f e m)
      (:metadata e)))
  clojure.core.protocols.CollReduce
  (coll-reduce [_this afn]
    (if-let [f (.-coll-reduce_afn e)]
      (f e m afn)
      (reduce afn m)))
  (coll-reduce [_this afn init]
    (if-let [f (.-coll-reduce_afn_init e)]
      (f e m afn init)
      (reduce afn init m)))
  clojure.core.protocols.IKVReduce
  (kv-reduce [_this afn init]
    (if-let [f (.-kv-reduce_afn_init e)]
      (f e m afn init)
      (reduce-kv afn init m)))
  Map
  (^int size [_this]
    (if-let [f (.-size e)]
      (f e m)
      (.count ^Counted m)))
  (^boolean isEmpty [_this]
    (if-let [f (.-isEmpty e)]
      (f e m)
      (zero? (.count ^Counted m))))
  (^boolean containsValue [_this v]
    (if-let [f (.-containsValue_v e)]
      (f e m v)
      (boolean (some #(= ^Object v %) (vals m)))))
  (^Object get [_this k]
    (if-let [f (.-get_k e)]
      (f e m k)
      (.valAt ^ILookup m k)))
  (^Set entrySet [_this]
    (if-let [f (.-entrySet e)]
      (f e m)
      (set (.seq ^Seqable m))))
  (^Set keySet [_this]
    (if-let [f (.-keySet e)]
      (f e m)
      (set (keys m))))
  (^Collection values [_this]
    (if-let [f (.-values e)]
      (f e m)
      (vals m)))
  (put [_this k v]
    (if-let [f (.-put_k_v e)]
      (f e m k v)
      (throw (UnsupportedOperationException. "put not supported on immutable WrapMap"))))
  (remove [_this k]
    (if-let [f (.-remove_k e)]
      (f e m k)
      (throw (UnsupportedOperationException. "remove not supported on immutable WrapMap"))))
  (putAll [_this mx]
    (if-let [f (.-putAll e)]
      (f e m mx)
      (throw (UnsupportedOperationException. "putAll not supported on immutable WrapMap"))))
  (clear [_this]
    (if-let [f (.-get e)]
      (f e m)
      (throw (UnsupportedOperationException. "clear not supported on immutable WrapMap")))))

(deftype WrapMap+valAt_k [^Impls e ^IPersistentMap m]
  IPersistentMap
  IHashEq
  MapEquivalence
  (hashCode [_this] (.hashCode ^Object m))
  (equiv [_this o]
    (if (instance? IPersistentMap o) (.equiv ^IPersistentMap m ^Object o) (.equals ^Object m ^Object o)))
  (hasheq [_this] (.hasheq ^IHashEq m))
  Object
  (equals [_this o] (.equals ^Object m ^Object o))
  (toString [_this] (if-let [f (.-toString e)] (f e m) (.toString ^Object m)))
  wc/IWrapAssociative
  (-assoc-impl [_this k v]
    (assert (allowable-impls k))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls (assoc e k v)) m)))
  (-contains-impl? [_this k] (get e k))
  (-impl [_this k] (get ^ILookup e k))
  (-get-impls [_this] e)
  (-with-wrap [_this new-impls]
    (assert (every? allowable-impls (keys new-impls)))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls new-impls) m)))
  (-vary [_this afn args]
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (let [new-impls (apply afn e args)]
        (assert (every? allowable-impls (keys new-impls)))
        (construct (map->Impls new-impls) m))))
  (-unwrap [_this] m)
  (-dissoc-impl [_this k]
    (assert (allowable-impls k))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls (assoc ^Impls e k nil)) m)))
  (-freeze [_this]
    (WrapMap+valAt_k. (assoc e :frozen? true) m))
  Associative
  (containsKey [_this k]
    (if-let [f (.-containsKey_k e)]
      (f e m k)
      ^boolean (.containsKey ^Associative m k)))
  (entryAt [_this k]
    (if-let [f (.-entryAt_k e)]
      (f e m k)
      (.entryAt ^Associative m k)))

  (assoc [_this k v]
    (WrapMap+valAt_k. e ^IPersistentMap (.assoc ^IPersistentMap m k v)))

  IKVReduce
  (kvreduce [_this f init]
    (if-let [f (.-kvreduce_f_init e)]
      (f e m f init)
      (reduce-kv f init m)))
  ILookup

  (valAt [_this k] ((.-valAt_k e) e m k))
  (valAt [_this k nf] ((.-valAt_k_nf e) e m k nf))

  IMapIterable
  (keyIterator [_this]
    (if-let [f (.-keyIterator e)]
      (f e m)
      (.keyIterator ^IMapIterable m)))
  (valIterator [_this]
    (if-let [f (.-valIterator e)]
      (f e m)
      (.valIterator ^IMapIterable m)))
  Counted
  (^int count [_this]
    (if-let [f (.-count e)]
      (f e m)
      (.count ^Counted m)))
  IPersistentCollection
  (^IPersistentCollection empty [_this]
    (if-let [f (.-empty e)]
      (f e m)
      (WrapMap+valAt_k. e (.empty ^IPersistentCollection m))))
  (^IPersistentCollection cons [_this v]
    (if-let [f (.-cons_v e)]
      (f e m v)
      (WrapMap+valAt_k. e ^IPersistentMap (.cons ^IPersistentCollection m v))))
  (^IPersistentMap assocEx [_this k v]
    (if-let [f (.-assocEx_k_v e)]
      (f e m k v)
      (WrapMap+valAt_k. e ^IPersistentMap (.assocEx ^IPersistentMap m k v))))
  (^IPersistentMap without [_this k]
    (if-let [f (.-without_k e)]
      (f e m k)
      (WrapMap+valAt_k. e ^IPersistentMap (.without ^IPersistentMap m k))))
  Seqable
  (seq [_this]
    (if-let [f (.-seq e)]
      (f e m)
      (.seq ^Seqable m)))
  Iterable
  (iterator [_this]
    (if-let [f (.-iterator e)]
      (f e m)
      (.iterator ^Iterable m)))
  IFn
  (invoke [_this] (wc/handle-invoke e m))
  (invoke [_this k] (wc/handle-invoke e m k))
  (invoke [_this k nf] (wc/handle-invoke e m k nf))
  (invoke [_this arg1 arg2 arg3] (wc/handle-invoke e m arg1 arg2 arg3))
  (invoke [_this arg1 arg2 arg3 arg4] (wc/handle-invoke e m arg1 arg2 arg3 arg4))
  (invoke [_this arg1 arg2 arg3 arg4 arg5] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args] (apply wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args))
  (applyTo [_this args] (apply wc/handle-invoke e m args))
  IEditableCollection
  (^ITransientCollection asTransient [_this]
    ((.-twrap e) e (transient m)))
  IObj
  (withMeta [_this new-meta]
    (if-let [f (.-withMeta_meta e)]
      (f e m new-meta)
      (WrapMap+valAt_k. (assoc e :metadata new-meta) m)))
  IMeta
  (meta [_this]
    (if-let [f (.-meta e)]
      (f e m)
      (:metadata e)))
  clojure.core.protocols.CollReduce
  (coll-reduce [_this afn]
    (if-let [f (.-coll-reduce_afn e)]
      (f e m afn)
      (reduce afn m)))
  (coll-reduce [_this afn init]
    (if-let [f (.-coll-reduce_afn_init e)]
      (f e m afn init)
      (reduce afn init m)))
  clojure.core.protocols.IKVReduce
  (kv-reduce [_this afn init]
    (if-let [f (.-kv-reduce_afn_init e)]
      (f e m afn init)
      (reduce-kv afn init m)))
  Map
  (^int size [_this]
    (if-let [f (.-size e)]
      (f e m)
      (.count ^Counted m)))
  (^boolean isEmpty [_this]
    (if-let [f (.-isEmpty e)]
      (f e m)
      (zero? (.count ^Counted m))))
  (^boolean containsValue [_this v]
    (if-let [f (.-containsValue_v e)]
      (f e m v)
      (boolean (some #(= ^Object v %) (vals m)))))
  (^Object get [_this k]
    (if-let [f (.-get_k e)]
      (f e m k)
      (.valAt ^ILookup m k)))
  (^Set entrySet [_this]
    (if-let [f (.-entrySet e)]
      (f e m)
      (set (.seq ^Seqable m))))
  (^Set keySet [_this]
    (if-let [f (.-keySet e)]
      (f e m)
      (set (keys m))))
  (^Collection values [_this]
    (if-let [f (.-values e)]
      (f e m)
      (vals m)))
  (put [_this k v]
    (if-let [f (.-put_k_v e)]
      (f e m k v)
      (throw (UnsupportedOperationException. "put not supported on immutable WrapMap"))))
  (remove [_this k]
    (if-let [f (.-remove_k e)]
      (f e m k)
      (throw (UnsupportedOperationException. "remove not supported on immutable WrapMap"))))
  (putAll [_this mx]
    (if-let [f (.-putAll e)]
      (f e m mx)
      (throw (UnsupportedOperationException. "putAll not supported on immutable WrapMap"))))
  (clear [_this]
    (if-let [f (.-get e)]
      (f e m)
      (throw (UnsupportedOperationException. "clear not supported on immutable WrapMap")))))

(deftype WrapMap+assoc_k_v|valAt_k [^Impls e ^IPersistentMap m]
  IPersistentMap
  IHashEq
  MapEquivalence
  (hashCode [_this] (.hashCode ^Object m))
  (equiv [_this o]
    (if (instance? IPersistentMap o) (.equiv ^IPersistentMap m ^Object o) (.equals ^Object m ^Object o)))
  (hasheq [_this] (.hasheq ^IHashEq m))
  Object
  (equals [_this o] (.equals ^Object m ^Object o))
  (toString [_this] (if-let [f (.-toString e)] (f e m) (.toString ^Object m)))
  wc/IWrapAssociative
  (-assoc-impl [_this k v]
    (assert (allowable-impls k))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls (assoc e k v)) m)))
  (-contains-impl? [_this k] (get e k))
  (-impl [_this k] (get ^ILookup e k))
  (-get-impls [_this] e)
  (-with-wrap [_this new-impls]
    (assert (every? allowable-impls (keys new-impls)))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls new-impls) m)))
  (-vary [_this afn args]
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (let [new-impls (apply afn e args)]
        (assert (every? allowable-impls (keys new-impls)))
        (construct (map->Impls new-impls) m))))
  (-unwrap [_this] m)
  (-dissoc-impl [_this k]
    (assert (allowable-impls k))
    (if (:frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls (assoc ^Impls e k nil)) m)))
  (-freeze [_this]
    (WrapMap+assoc_k_v|valAt_k. (assoc e :frozen? true) m))
  Associative
  (containsKey [_this k]
    (if-let [f (.-containsKey_k e)]
      (f e m k)
      ^boolean (.containsKey ^Associative m k)))
  (entryAt [_this k]
    (if-let [f (.-entryAt_k e)]
      (f e m k)
      (.entryAt ^Associative m k)))

  (assoc [_this k v] ((.-assoc_k_v e) e m k v))

  IKVReduce
  (kvreduce [_this f init]
    (if-let [f (.-kvreduce_f_init e)]
      (f e m f init)
      (reduce-kv f init m)))
  ILookup

  (valAt [_this k] ((.-valAt_k e) e m k))
  (valAt [_this k nf] ((.-valAt_k_nf e) e m k nf))

  IMapIterable
  (keyIterator [_this]
    (if-let [f (.-keyIterator e)]
      (f e m)
      (.keyIterator ^IMapIterable m)))
  (valIterator [_this]
    (if-let [f (.-valIterator e)]
      (f e m)
      (.valIterator ^IMapIterable m)))
  Counted
  (^int count [_this]
    (if-let [f (.-count e)]
      (f e m)
      (.count ^Counted m)))
  IPersistentCollection
  (^IPersistentCollection empty [_this]
    (if-let [f (.-empty e)]
      (f e m)
      (WrapMap+assoc_k_v|valAt_k. e (.empty ^IPersistentCollection m))))
  (^IPersistentCollection cons [_this v]
    (if-let [f (.-cons_v e)]
      (f e m v)
      (WrapMap+assoc_k_v|valAt_k. e ^IPersistentMap (.cons ^IPersistentCollection m v))))
  (^IPersistentMap assocEx [_this k v]
    (if-let [f (.-assocEx_k_v e)]
      (f e m k v)
      (WrapMap+assoc_k_v|valAt_k. e ^IPersistentMap (.assocEx ^IPersistentMap m k v))))
  (^IPersistentMap without [_this k]
    (if-let [f (.-without_k e)]
      (f e m k)
      (WrapMap+assoc_k_v|valAt_k. e ^IPersistentMap (.without ^IPersistentMap m k))))
  Seqable
  (seq [_this]
    (if-let [f (.-seq e)]
      (f e m)
      (.seq ^Seqable m)))
  Iterable
  (iterator [_this]
    (if-let [f (.-iterator e)]
      (f e m)
      (.iterator ^Iterable m)))
  IFn
  (invoke [_this] (wc/handle-invoke e m))
  (invoke [_this k] (wc/handle-invoke e m k))
  (invoke [_this k nf] (wc/handle-invoke e m k nf))
  (invoke [_this arg1 arg2 arg3] (wc/handle-invoke e m arg1 arg2 arg3))
  (invoke [_this arg1 arg2 arg3 arg4] (wc/handle-invoke e m arg1 arg2 arg3 arg4))
  (invoke [_this arg1 arg2 arg3 arg4 arg5] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20] (wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20))
  (invoke [_this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args] (apply wc/handle-invoke e m arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19 arg20 rest-args))
  (applyTo [_this args] (apply wc/handle-invoke e m args))
  IEditableCollection
  (^ITransientCollection asTransient [_this]
    ((.-twrap e) e (transient m)))
  IObj
  (withMeta [_this new-meta]
    (if-let [f (.-withMeta_meta e)]
      (f e m new-meta)
      (WrapMap+assoc_k_v|valAt_k. (assoc e :metadata new-meta) m)))
  IMeta
  (meta [_this]
    (if-let [f (.-meta e)]
      (f e m)
      (:metadata e)))
  clojure.core.protocols.CollReduce
  (coll-reduce [_this afn]
    (if-let [f (.-coll-reduce_afn e)]
      (f e m afn)
      (reduce afn m)))
  (coll-reduce [_this afn init]
    (if-let [f (.-coll-reduce_afn_init e)]
      (f e m afn init)
      (reduce afn init m)))
  clojure.core.protocols.IKVReduce
  (kv-reduce [_this afn init]
    (if-let [f (.-kv-reduce_afn_init e)]
      (f e m afn init)
      (reduce-kv afn init m)))
  Map
  (^int size [_this]
    (if-let [f (.-size e)]
      (f e m)
      (.count ^Counted m)))
  (^boolean isEmpty [_this]
    (if-let [f (.-isEmpty e)]
      (f e m)
      (zero? (.count ^Counted m))))
  (^boolean containsValue [_this v]
    (if-let [f (.-containsValue_v e)]
      (f e m v)
      (boolean (some #(= ^Object v %) (vals m)))))
  (^Object get [_this k]
    (if-let [f (.-get_k e)]
      (f e m k)
      (.valAt ^ILookup m k)))
  (^Set entrySet [_this]
    (if-let [f (.-entrySet e)]
      (f e m)
      (set (.seq ^Seqable m))))
  (^Set keySet [_this]
    (if-let [f (.-keySet e)]
      (f e m)
      (set (keys m))))
  (^Collection values [_this]
    (if-let [f (.-values e)]
      (f e m)
      (vals m)))
  (put [_this k v]
    (if-let [f (.-put_k_v e)]
      (f e m k v)
      (throw (UnsupportedOperationException. "put not supported on immutable WrapMap"))))
  (remove [_this k]
    (if-let [f (.-remove_k e)]
      (f e m k)
      (throw (UnsupportedOperationException. "remove not supported on immutable WrapMap"))))
  (putAll [_this mx]
    (if-let [f (.-putAll e)]
      (f e m mx)
      (throw (UnsupportedOperationException. "putAll not supported on immutable WrapMap"))))
  (clear [_this]
    (if-let [f (.-get e)]
      (f e m)
      (throw (UnsupportedOperationException. "clear not supported on immutable WrapMap")))))

(defn get-wrap-persistent [e]
  (let [assoc_k_v? (.-assoc_k_v e)
        valAt_k? (.-valAt_k e)
        valAt_k_nf? (.-valAt_k_nf e)]
    (if (and assoc_k_v? (not valAt_k?))
      ->WrapMap+assoc_k_v
      (if (and valAt_k? valAt_k_nf? (not assoc_k_v?))
        ->WrapMap+valAt_k
        (if (and assoc_k_v? valAt_k?)
          ->WrapMap+assoc_k_v|valAt_k
          ->WrapMap)))))

(defn get-wrap-transient [e]
  (if (and (.-T_assoc_k_v e) (.-T_valAt_k e))
    ->TransientWrapMap+assoc_k_v|valAt_k
    (if (.-T_assoc_k_v e)
      ->TransientWrapMap+assoc_k_v
      (if (.-T_valAt_k e)
        ->TransientWrapMap+valAt_k
        ->TransientWrapMap))))

(defn construct [e m & [transient?]]
  (if-not (instance? Impls e)
    (construct (map->Impls e) m)
    (let [wrap-persistent (get-wrap-persistent e)
          wrap-transient (get-wrap-transient e)
          new-impls (assoc e
                           :<- wrap-persistent
                           :pwrap wrap-persistent
                           :twrap wrap-transient)]
      (if transient?
        (wrap-transient new-impls m)
        (wrap-persistent new-impls m)))))

;;----------------------------------------------------------------------
;; Printing
;;----------------------------------------------------------------------

(defmethod print-method WrapMap [^WrapMap pm ^Writer w]
  (let [^IPersistentMap m (.m pm)
        ^Impls e (.e pm)]
    (if-let [f (.-print-method_writer e)]
      (f e m w)
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

(defn wrap-map*
  "Internal raw constructor for WrapMap. Creates a WrapMap instance
  directly from the underlying collection `m` and environment map
  `e`. Does NOT perform the internal preparation step (like
  ensuring default-invoke). Prefer `com.jolygon.wrap-map/wrap`
  for general use."
  ^WrapMap
  ([] (construct empty-impls {}))
  ^WrapMap
  ([^IPersistentMap m] (construct empty-impls m))
  ^WrapMap
  ([^Impls impls ^IPersistentMap m] (construct impls m)))
