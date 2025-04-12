(ns com.jolygon.poly-map.api-0.impl
  "Internal implementation details for PolyMap (ClojureScript).
  Provides the PolyMap and TransientPolyMap deftypes and related protocols.
  Users should generally prefer the API functions in 'com.jolygon.poly-map.api-0'
  or subsequent API versions, and use the implementation keys defined in
  'com.jolygon.poly-map.api-0.keys' and 'com.jolygon.poly-map.api-0.trans.keys'.

  This namespace is subject to change."
  (:require
   [com.jolygon.poly-map.api-0.keys :as pm]
   [com.jolygon.poly-map.api-0.trans.keys :as tpm]))

;;----------------------------------------------------------------------
;; Transient Implementation
;;----------------------------------------------------------------------

(declare PolyMap ->TransientPolyMap)

(defn- ^:private ensure-editable
  "Throws an exception if the transient's editable flag is false."
  [^boolean edit]
  (when-not edit
    (throw (js/Error. "Transient used after persistent! call"))))

(deftype TransientPolyMap [^:mutable ^boolean edit
                           ^:ITransientMap t_m
                           ^:IPersistentMap impls
                           ^:IPersistentMap metadata]
  ITransientCollection
  (^TransientPolyMap -conj! [this entry]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/conj_entry)]
      (f this t_m impls metadata entry)
      (->TransientPolyMap edit (-conj! t_m entry) impls metadata)))
  (^PolyMap -persistent! [this]
    (ensure-editable edit)
    (set! edit false)
    (if-let [f (get impls ::tpm/persistent)]
      (f this t_m impls metadata)
      (let [pm (-persistent! t_m)
            [_ ^IPersistentMap pe ^IPersistentMap nm ^IPersistentMap m]
            (pm/construct this pm impls metadata)]
        (PolyMap. pe nm m))))
  ITransientAssociative
  (^TransientPolyMap -assoc! [this k v]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/-assoc!_k_v)]
      (f this t_m impls metadata k v)
      (->TransientPolyMap edit (-assoc! t_m k v) impls metadata)))
  ITransientMap
  (^TransientPolyMap -dissoc! [this k]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/-dissoc!_k)]
      (f this t_m impls metadata k)
      (->TransientPolyMap edit (-dissoc! t_m k) impls metadata)))
  ILookup
  (-lookup [this k]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/valAt_k)]
      (f this t_m impls metadata k)
      (-lookup t_m k)))
  (-lookup [this k nf]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/valAt_k_nf)]
      (f this t_m impls metadata k nf)
      (-lookup t_m k nf)))
  ICounted
  (^number -count [this]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/count)]
      (f this t_m impls metadata)
      (-count t_m))))

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

(deftype PolyMap [^:IPersistentMap m
                  ^:IPersistentMap impls
                  ^:IPersistentMap metadata]
  Object
  (toString [this]
    (if-let [f (get impls ::pm/toString)]
      (f this m impls metadata)
      (pr-str* m)))
  IHash
  (-hash [_this]
    (-hash m))
  IEquiv
  (-equiv [_this other]
    (-equiv m other))
  IFn
  (-invoke [this] (apply pm/handle-invoke this m impls metadata []))
  (-invoke [this k] (apply pm/handle-invoke this m impls metadata [k]))
  (-invoke [this k nf] (apply pm/handle-invoke this m impls metadata [k nf]))
  (-invoke [this a b c] (apply pm/handle-invoke this m impls metadata [a b c]))
  (-invoke [this a b c d] (apply pm/handle-invoke this m impls metadata [a b c d]))
  (-invoke [this a b c d e] (apply pm/handle-invoke this m impls metadata [a b c d e]))
  (-invoke [this a b c d e f] (apply pm/handle-invoke this m impls metadata [a b c d e f]))
  (-invoke [this a b c d e f g] (apply pm/handle-invoke this m impls metadata [a b c d e f g]))
  (-invoke [this a b c d e f g h] (apply pm/handle-invoke this m impls metadata [a b c d e f g h]))
  (-invoke [this a b c d e f g h i] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i]))
  (-invoke [this a b c d e f g h i j] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j]))
  (-invoke [this a b c d e f g h i j k] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k]))
  (-invoke [this a b c d e f g h i j k l] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k l]))
  (-invoke [this a b c d e f g h i j k l m] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k l m]))
  (-invoke [this a b c d e f g h i j k l m n] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k l m n]))
  (-invoke [this a b c d e f g h i j k l m n o] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k l m n o]))
  (-invoke [this a b c d e f g h i j k l m n o p] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k l m n o p]))
  (-invoke [this a b c d e f g h i j k l m n o p q] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k l m n o p q]))
  (-invoke [this a b c d e f g h i j k l m n o p q r] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k l m n o p q r]))
  (-invoke [this a b c d e f g h i j k l m n o p q r s] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k l m n o p q r s]))
  (-invoke [this a b c d e f g h i j k l m n o p q r s t] (apply pm/handle-invoke this m impls metadata [a b c d e f g h i j k l m n o p q r s t]))
  (-invoke [this a b c d e f g h i j k l m n o p q r s t rest] (apply pm/handle-invoke this m impls metadata (concat [a b c d e f g h i j k l m n o p q r s t] rest)))
  ICollection
  (^PolyMap -conj [this entry]
    (if-let [f (get impls ::pm/-cons_v)]
      (f this m impls metadata entry)
      (let [[_ ^IPersistentMap pe ^IPersistentMap nm ^IPersistentMap m]
            (pm/construct this (-conj m entry) impls metadata)]
        (PolyMap. pe nm m))))
  IEmptyableCollection
  (^PolyMap -empty [this]
    (if-let [f (get impls ::pm/-empty)]
      (f this m impls metadata)
      (PolyMap. (-empty m) impls metadata)))
  IMap
  (^PolyMap -dissoc [this k]
    (if-let [f (get impls ::pm/-without_k)]
      (f this m impls metadata k)
      (let [[_ ^IPersistentMap pe ^IPersistentMap nm ^IPersistentMap m]
            (pm/construct this (-dissoc m k) impls metadata)]
        (PolyMap. pe nm m))))
  IAssociative
  (^PolyMap -assoc [this k v]
    (if-let [f (get impls ::pm/-assoc_k_v)]
      (f this m impls metadata k v)
      (let [[_ ^IPersistentMap pe ^IPersistentMap nm ^IPersistentMap m]
            (pm/construct this (-assoc m k v) impls metadata)]
        (PolyMap. pe nm m))))
  (^boolean -contains-key? [this k]
    (if-let [f (get impls ::pm/-contains-key?_k)]
      (f this m impls metadata k)
      (-contains-key? m k)))
  IFind
  (-find [this k]
    (if-let [f (get impls ::pm/-find_k)]
      (f this m impls metadata k)
      (-find m k)))
  ISeqable
  (-seq [this]
    (if-let [f (get impls ::pm/-seq)]
      (f this m impls metadata)
      (-seq m)))
  IIterable
  IMeta
  (-meta [this]
    (if-let [f (get impls ::pm/-meta)]
      (f this m impls metadata)
      metadata))
  IWithMeta
  (^PolyMap -with-meta [this ^:IPersistentMap new-meta]
    (if-let [f (get impls ::pm/withMeta_meta)]
      (f this m impls metadata new-meta)
      (if (identical? new-meta metadata)
        this
        (PolyMap. m impls new-meta))))
  ICounted
  (^number -count [this]
    (if-let [f (get impls ::pm/count)]
      (f this m impls metadata)
      (-count m)))
  ILookup
  (-lookup [this k]
    (if-let [f (get impls ::pm/-lookup_k)]
      (f this m impls metadata k)
      (-lookup m k)))
  (-lookup [this k nf]
    (if-let [f (get impls ::pm/-lookup_k_nf)]
      (f this m impls metadata k nf)
      (-lookup m k nf)))
  IKVReduce
  (-kv-reduce [this f init]
    (if-let [reduce-fn (get impls ::pm/kv-reduce_f_init)]
      (reduce-fn this m impls metadata f init)
      (-kv-reduce m f init)))
  pm/IPolyAssociative
  (^PolyMap -assoc-impl [_this k v]
    (PolyMap. m (-assoc impls k v) metadata))
  (^boolean -contains-impl? [_this k]
    (-contains-key? impls k))
  (-impl [_this k]
    (-lookup impls k))
  (^IPersistentMap -get-impls [_this]
    impls)
  (^PolyMap -set-impls [_this ^:IPersistentMap new-impls]
    (PolyMap. m new-impls metadata))
  (^IPersistentMap -get-coll [_this]
    m)
  (^PolyMap -dissoc-impl [_this k]
    (PolyMap. m (-dissoc impls k) metadata))
  IEditableCollection
  (^TransientPolyMap -as-transient [this]
    (if-let [f (get impls ::pm/-as-transient)]
      (f this m impls metadata)
      (TransientPolyMap. true (transient m) impls metadata)))
  IPrintWithWriter
  (-pr-writer [this writer opts]
    (let [m        (.-m this)
          impls    (.-impls this)
          metadata (.-metadata this)]
      (if-let [f (get impls ::pm/-pr-writer_writer_opts)]
        (f this m impls metadata writer opts)
        (print-map this pr-writer writer opts)))))

;;----------------------------------------------------------------------
;; Static Methods / Setup for PolyMap
;;----------------------------------------------------------------------

(set! (.-EMPTY PolyMap) (PolyMap. {} {} {}))

(set! (.-fromArray PolyMap)
      (fn [arr ^boolean no-clone]
        (let [arr (if no-clone arr (.slice arr))
              len (.-length arr)]
          (loop [i 0
                 ret (.asTransient (.-EMPTY PolyMap))]
            (if (< i len)
              (recur (inc i) (-assoc! ret (aget arr i) (aget arr (inc i))))
              (-persistent! ret))))))

;;----------------------------------------------------------------------
;; Constructor
;;----------------------------------------------------------------------

(defn poly-map*
  "Internal raw constructor (CLJS). Prefer API functions."
  (^PolyMap []
   (PolyMap. {} {} {}))
  (^PolyMap [m]
   (PolyMap. m {} {}))
  (^PolyMap [m impls]
   (PolyMap. m impls {}))
  (^PolyMap [m impls metadata]
   (PolyMap. m impls metadata)))
