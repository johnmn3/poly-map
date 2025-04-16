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

(declare PolyMap PolyMap+-lookup_k ->TransientPolyMap PolyMap+-assoc_k_v
         PolyMap+-assoc_k_v|-lookup_k reproduce ->PolyMap)

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
    ;; (ensure-editable edit)
    (if-let [f (get impls ::tpm/conj_entry)]
      (f this t_m impls metadata entry)
      (TransientPolyMap. edit (-conj! t_m entry) impls metadata)))
  (^PolyMap -persistent! [_this]
    (ensure-editable edit)
    (set! edit false)
    (if (and (::pm/-assoc_k_v impls) (::pm/-lookup_k impls))
      (PolyMap+-assoc_k_v|-lookup_k. ^IPersistentMap (-persistent! t_m) impls metadata)
      (if (::pm/-assoc_k_v impls)
        (PolyMap+-assoc_k_v. ^IPersistentMap (-persistent! t_m) impls metadata)
        (if (::pm/-lookup_k impls)
          (PolyMap+-lookup_k. ^IPersistentMap (-persistent! t_m) impls metadata)
          (PolyMap. ^IPersistentMap (-persistent! t_m) impls metadata)))))
  ITransientAssociative
  (^TransientPolyMap -assoc! [_this k v]
    ;; (ensure-editable edit)
    (TransientPolyMap. edit (-assoc! t_m k v) impls metadata))
  ITransientMap
  (^TransientPolyMap -dissoc! [this k]
    ;; (ensure-editable edit)
    (if-let [f (get impls ::tpm/-dissoc!_k)]
      (f this t_m impls metadata k)
      (TransientPolyMap. edit (-dissoc! t_m k) impls metadata)))
  ILookup
  (-lookup [_this k]
    ;; (ensure-editable edit)
    (-lookup t_m k))
  (-lookup [_this k nf]
    ;; (ensure-editable edit)
    (-lookup t_m k nf))
  ICounted
  (^number -count [this]
    ;; (ensure-editable edit)
    (if-let [f (get impls ::tpm/count)]
      (f this t_m impls metadata)
      (-count t_m))))

(deftype TransientPolyMap+-assoc!_k_v
         [^:mutable ^boolean edit
          ^:ITransientMap t_m
          ^:IPersistentMap impls
          ^:IPersistentMap metadata]
  ITransientCollection
  (^TransientPolyMap+-assoc!_k_v -conj! [this entry]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/conj_entry)]
      (f this t_m impls metadata entry)
      (TransientPolyMap+-assoc!_k_v. edit (-conj! t_m entry) impls metadata)))
  (^PolyMap -persistent! [_this]
    (ensure-editable edit)
    (set! edit false)
    (if (and (::pm/-assoc_k_v impls) (::pm/-lookup_k impls))
      (PolyMap+-assoc_k_v|-lookup_k. ^IPersistentMap (-persistent! t_m) impls metadata)
      (if (::pm/-assoc_k_v impls)
        (PolyMap+-assoc_k_v. ^IPersistentMap (-persistent! t_m) impls metadata)
        (if (::pm/-lookup_k impls)
          (PolyMap+-lookup_k. ^IPersistentMap (-persistent! t_m) impls metadata)
          (PolyMap. ^IPersistentMap (-persistent! t_m) impls metadata)))))
  ITransientAssociative
  (^TransientPolyMap+-assoc!_k_v -assoc! [this k v]
    (ensure-editable edit)
    ((get impls ::tpm/-assoc!_k_v) this t_m impls metadata k v))
  ITransientMap
  (^TransientPolyMap+-assoc!_k_v -dissoc! [this k]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/-dissoc!_k)]
      (f this t_m impls metadata k)
      (TransientPolyMap+-assoc!_k_v. edit (-dissoc! t_m k) impls metadata)))
  ILookup
  (-lookup [_this k]
    (ensure-editable edit)
    (-lookup t_m k))
  (-lookup [_this k nf]
    (ensure-editable edit)
    (-lookup t_m k nf))
  ICounted
  (^number -count [this]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/count)]
      (f this t_m impls metadata)
      (-count t_m))))

(deftype TransientPolyMap+-lookup_k
         [^:mutable ^boolean edit
          ^:ITransientMap t_m
          ^:IPersistentMap impls
          ^:IPersistentMap metadata]
  ITransientCollection
  (^TransientPolyMap+-lookup_k -conj! [this entry]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/conj_entry)]
      (f this t_m impls metadata entry)
      (TransientPolyMap+-lookup_k. edit (-conj! t_m entry) impls metadata)))
  (^PolyMap -persistent! [_this]
    (ensure-editable edit)
    (set! edit false)
    (if (and (::pm/-assoc_k_v impls) (::pm/-lookup_k impls))
      (PolyMap+-assoc_k_v|-lookup_k. ^IPersistentMap (-persistent! t_m) impls metadata)
      (if (::pm/-assoc_k_v impls)
        (PolyMap+-assoc_k_v. ^IPersistentMap (-persistent! t_m) impls metadata)
        (if (::pm/-lookup_k impls)
          (PolyMap+-lookup_k. ^IPersistentMap (-persistent! t_m) impls metadata)
          (PolyMap. ^IPersistentMap (-persistent! t_m) impls metadata)))))
  ITransientAssociative
  (^TransientPolyMap+-lookup_k -assoc! [_this k v]
    (ensure-editable edit)
    (TransientPolyMap+-lookup_k. edit (-assoc! t_m k v) impls metadata))
  ITransientMap
  (^TransientPolyMap+-lookup_k -dissoc! [this k]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/-dissoc!_k)]
      (f this t_m impls metadata k)
      (TransientPolyMap+-lookup_k. edit (-dissoc! t_m k) impls metadata)))
  ILookup
  (-lookup [this k]
    (ensure-editable edit)
    ((get impls ::tpm/-lookup_k) this t_m impls metadata k))
  (-lookup [this k nf]
    (ensure-editable edit)
    ((get impls ::tpm/-lookup_k_nf) this t_m impls metadata k nf))
  ICounted
  (^number -count [this]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/count)]
      (f this t_m impls metadata)
      (-count t_m))))

(deftype TransientPolyMap+-assoc!_k_v|-lookup_k
         [^:mutable ^boolean edit
          ^:ITransientMap t_m
          ^:IPersistentMap impls
          ^:IPersistentMap metadata]
  ITransientCollection
  (^TransientPolyMap+-assoc!_k_v|-lookup_k -conj! [this entry]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/conj_entry)]
      (f this t_m impls metadata entry)
      (TransientPolyMap+-assoc!_k_v|-lookup_k. edit (-conj! t_m entry) impls metadata)))
  (^PolyMap -persistent! [_this]
    (ensure-editable edit)
    (set! edit false)
    (if (and (::pm/-assoc_k_v impls) (::pm/-lookup_k impls))
      (PolyMap+-assoc_k_v|-lookup_k. ^IPersistentMap (-persistent! t_m) impls metadata)
      (if (::pm/-assoc_k_v impls)
        (PolyMap+-assoc_k_v. ^IPersistentMap (-persistent! t_m) impls metadata)
        (if (::pm/-lookup_k impls)
          (PolyMap+-lookup_k. ^IPersistentMap (-persistent! t_m) impls metadata)
          (PolyMap. ^IPersistentMap (-persistent! t_m) impls metadata)))))
  ITransientAssociative
  (^TransientPolyMap+-assoc!_k_v|-lookup_k -assoc! [this k v]
    (ensure-editable edit)
    ((get impls ::tpm/-assoc!_k_v) this t_m impls metadata k v))
  ITransientMap
  (^TransientPolyMap+-assoc!_k_v|-lookup_k -dissoc! [this k]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/-dissoc!_k)]
      (f this t_m impls metadata k)
      (TransientPolyMap+-assoc!_k_v|-lookup_k. edit (-dissoc! t_m k) impls metadata)))
  ILookup
  (-lookup [this k]
    (ensure-editable edit)
    ((get impls ::tpm/-lookup_k) this t_m impls metadata k))
  (-lookup [this k nf]
    (ensure-editable edit)
    ((get impls ::tpm/-lookup_k_nf) this t_m impls metadata k nf))
  ICounted
  (^number -count [this]
    (ensure-editable edit)
    (if-let [f (get impls ::tpm/count)]
      (f this t_m impls metadata)
      (-count t_m))))

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
  (-invoke [this a b c d e f g h i j k l m n o p q r s t the-rest] (apply pm/handle-invoke this m impls metadata (concat [a b c d e f g h i j k l m n o p q r s t] the-rest)))
  ICollection
  (^PolyMap -conj [this entry]
    (if-let [f (get impls ::pm/-cons_v)]
      (f this m impls metadata entry)
      (PolyMap. (-conj m entry) impls metadata)))
  IEmptyableCollection
  (^PolyMap -empty [this]
    (if-let [f (get impls ::pm/-empty)]
      (f this m impls metadata)
      (PolyMap. (-empty m) impls metadata)))
  IMap
  (^PolyMap -dissoc [this k]
    (if-let [f (get impls ::pm/-without_k)]
      (f this m impls metadata k)
      (PolyMap. (-dissoc m k) impls metadata)))

  IAssociative
  (^PolyMap -assoc [_this k v] (PolyMap. (-assoc m k v) impls metadata))

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
  (-lookup [_this k] (-lookup m k))
  (-lookup [_this k nf] (-lookup m k nf))

  IKVReduce
  (-kv-reduce [this f init]
    (if-let [reduce-fn (get impls ::pm/kv-reduce_f_init)]
      (reduce-fn this m impls metadata f init)
      (-kv-reduce m f init)))
  pm/IPolyAssociative
  (-assoc-impl [_this k v]
    (reproduce m (-assoc impls k v) metadata))
  (^boolean -contains-impl? [_this k]
    (-contains-key? impls k))
  (-impl [_this k]
    (-lookup impls k))
  (^IPersistentMap -get-impls [_this]
    impls)
  (-set-impls [_this ^:IPersistentMap new-impls]
    (reproduce m new-impls metadata))
  (^IPersistentMap -get-coll [_this]
    m)
  (-dissoc-impl [_this k]
    (reproduce m (-dissoc impls k) metadata))
  IEditableCollection
  (-as-transient [_this]
    (if (and (::tpm/-assoc!_k_v impls) (::tpm/-lookup_k impls))
      (TransientPolyMap+-assoc!_k_v|-lookup_k. true (transient m) impls metadata)
      (if (::tpm/-assoc!_k_v impls)
        (TransientPolyMap+-assoc!_k_v. true (transient m) impls metadata)
        (if (::tpm/-lookup_k impls)
          (TransientPolyMap+-lookup_k. true (transient m) impls metadata)
          (TransientPolyMap. true (transient m) impls metadata)))))
  IPrintWithWriter
  (-pr-writer [this writer opts]
    (let [m (.-m this)
          impls (.-impls this)
          metadata (.-metadata this)]
      (if-let [f (get impls ::pm/-pr-writer_writer_opts)]
        (f this m impls metadata writer opts)
        #_{:clj-kondo/ignore [:private-call]}
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
;; Persistent Implementation
;;----------------------------------------------------------------------

(deftype PolyMap+-assoc_k_v
         [^:IPersistentMap m
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
  (-invoke [this a b c d e f g h i j k l m n o p q r s t the-rest] (apply pm/handle-invoke this m impls metadata (concat [a b c d e f g h i j k l m n o p q r s t] the-rest)))
  ICollection
  (^PolyMap+-assoc_k_v -conj [this entry]
    (if-let [f (get impls ::pm/-cons_v)]
      (f this m impls metadata entry)
      (PolyMap+-assoc_k_v. (-conj m entry) impls metadata)))
  IEmptyableCollection
  (^PolyMap+-assoc_k_v -empty [this]
    (if-let [f (get impls ::pm/-empty)]
      (f this m impls metadata)
      (PolyMap+-assoc_k_v. (-empty m) impls metadata)))
  IMap
  (^PolyMap+-assoc_k_v -dissoc [this k]
    (if-let [f (get impls ::pm/-without_k)]
      (f this m impls metadata k)
      (PolyMap+-assoc_k_v. (-dissoc m k) impls metadata)))

  IAssociative
  (^PolyMap+-assoc_k_v -assoc [this k v]
    ((get impls ::pm/-assoc_k_v) this m impls metadata k v))

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
  (^PolyMap+-assoc_k_v -with-meta [this ^:IPersistentMap new-meta]
    (if-let [f (get impls ::pm/withMeta_meta)]
      (f this m impls metadata new-meta)
      (if (identical? new-meta metadata)
        this
        (PolyMap+-assoc_k_v. m impls new-meta))))
  ICounted
  (^number -count [this]
    (if-let [f (get impls ::pm/count)]
      (f this m impls metadata)
      (-count m)))
  ILookup
  (-lookup [_this k]
    (-lookup m k))
  (-lookup [_this k nf]
    (-lookup m k nf))
  IKVReduce
  (-kv-reduce [this f init]
    (if-let [reduce-fn (get impls ::pm/kv-reduce_f_init)]
      (reduce-fn this m impls metadata f init)
      (-kv-reduce m f init)))
  pm/IPolyAssociative
  (-assoc-impl [_this k v]
    (reproduce m (-assoc impls k v) metadata))
  (^boolean -contains-impl? [_this k]
    (-contains-key? impls k))
  (-impl [_this k]
    (-lookup impls k))
  (^IPersistentMap -get-impls [_this]
    impls)
  (-set-impls [_this ^:IPersistentMap new-impls]
    (reproduce m new-impls metadata))
  (^IPersistentMap -get-coll [_this]
    m)
  (-dissoc-impl [_this k]
    (reproduce m (-dissoc impls k) metadata))
  IEditableCollection
  (-as-transient [_this]
    (if (and (::tpm/-assoc!_k_v impls) (::tpm/-lookup_k impls))
      (TransientPolyMap+-assoc!_k_v|-lookup_k. true (transient m) impls metadata)
      (if (::tpm/-assoc!_k_v impls)
        (TransientPolyMap+-assoc!_k_v. true (transient m) impls metadata)
        (if (::tpm/-lookup_k impls)
          (TransientPolyMap+-lookup_k. true (transient m) impls metadata)
          (TransientPolyMap. true (transient m) impls metadata)))))
  IPrintWithWriter
  (-pr-writer [this writer opts]
    (let [m (.-m this)
          impls (.-impls this)
          metadata (.-metadata this)]
      (if-let [f (get impls ::pm/-pr-writer_writer_opts)]
        (f this m impls metadata writer opts)
        #_{:clj-kondo/ignore [:private-call]}
        (print-map this pr-writer writer opts)))))

;;----------------------------------------------------------------------
;; Static Methods / Setup for PolyMap
;;----------------------------------------------------------------------

(set! (.-EMPTY PolyMap+-assoc_k_v) (PolyMap+-assoc_k_v. {} {} {}))

(set! (.-fromArray PolyMap+-assoc_k_v)
      (fn [arr ^boolean no-clone]
        (let [arr (if no-clone arr (.slice arr))
              len (.-length arr)]
          (loop [i 0
                 ret (.asTransient (.-EMPTY PolyMap+-assoc_k_v))]
            (if (< i len)
              (recur (inc i) (-assoc! ret (aget arr i) (aget arr (inc i))))
              (-persistent! ret))))))

;;----------------------------------------------------------------------
;; Persistent Implementation
;;----------------------------------------------------------------------

(deftype PolyMap+-lookup_k
         [^:IPersistentMap m
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
  (-invoke [this a b c d e f g h i j k l m n o p q r s t the-rest] (apply pm/handle-invoke this m impls metadata (concat [a b c d e f g h i j k l m n o p q r s t] the-rest)))
  ICollection
  (^PolyMap+-lookup_k -conj [this entry]
    (if-let [f (get impls ::pm/-cons_v)]
      (f this m impls metadata entry)
      (PolyMap+-lookup_k. (-conj m entry) impls metadata)))
  IEmptyableCollection
  (^PolyMap+-lookup_k -empty [this]
    (if-let [f (get impls ::pm/-empty)]
      (f this m impls metadata)
      (PolyMap+-lookup_k. (-empty m) impls metadata)))
  IMap
  (^PolyMap+-lookup_k -dissoc [this k]
    (if-let [f (get impls ::pm/-without_k)]
      (f this m impls metadata k)
      (PolyMap+-lookup_k. (-dissoc m k) impls metadata)))
  IAssociative
  (^PolyMap+-lookup_k -assoc [_this k v]
    (PolyMap+-lookup_k. (-assoc m k v) impls metadata))
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
  (^PolyMap+-lookup_k -with-meta [this ^:IPersistentMap new-meta]
    (if-let [f (get impls ::pm/withMeta_meta)]
      (f this m impls metadata new-meta)
      (if (identical? new-meta metadata)
        this
        (PolyMap+-lookup_k. m impls new-meta))))
  ICounted
  (^number -count [this]
    (if-let [f (get impls ::pm/count)]
      (f this m impls metadata)
      (-count m)))
  ILookup
  (-lookup [this k]
    ((get impls ::pm/-lookup_k) this m impls metadata k))
  (-lookup [this k nf]
    ((get impls ::pm/-lookup_k_nf) this m impls metadata k nf))
  IKVReduce
  (-kv-reduce [this f init]
    (if-let [reduce-fn (get impls ::pm/kv-reduce_f_init)]
      (reduce-fn this m impls metadata f init)
      (-kv-reduce m f init)))
  pm/IPolyAssociative
  (-assoc-impl [_this k v]
    (reproduce m (-assoc impls k v) metadata))
  (^boolean -contains-impl? [_this k]
    (-contains-key? impls k))
  (-impl [_this k]
    (-lookup impls k))
  (^IPersistentMap -get-impls [_this]
    impls)
  (-set-impls [_this ^:IPersistentMap new-impls]
    (reproduce m new-impls metadata))
  (^IPersistentMap -get-coll [_this]
    m)
  (-dissoc-impl [_this k]
    (reproduce m (-dissoc impls k) metadata))
  IEditableCollection
  (-as-transient [_this]
    (if (and (::tpm/-assoc!_k_v impls) (::tpm/-lookup_k impls))
      (TransientPolyMap+-assoc!_k_v|-lookup_k. true (transient m) impls metadata)
      (if (::tpm/-assoc!_k_v impls)
        (TransientPolyMap+-assoc!_k_v. true (transient m) impls metadata)
        (if (::tpm/-lookup_k impls)
          (TransientPolyMap+-lookup_k. true (transient m) impls metadata)
          (TransientPolyMap. true (transient m) impls metadata)))))
  IPrintWithWriter
  (-pr-writer [this writer opts]
    (let [m (.-m this)
          impls (.-impls this)
          metadata (.-metadata this)]
      (if-let [f (get impls ::pm/-pr-writer_writer_opts)]
        (f this m impls metadata writer opts)
        #_{:clj-kondo/ignore [:private-call]}
        (print-map this pr-writer writer opts)))))

;;----------------------------------------------------------------------
;; Static Methods / Setup for PolyMap
;;----------------------------------------------------------------------

(set! (.-EMPTY PolyMap) (PolyMap+-lookup_k. {} {} {}))

(set! (.-fromArray PolyMap+-lookup_k)
      (fn [arr ^boolean no-clone]
        (let [arr (if no-clone arr (.slice arr))
              len (.-length arr)]
          (loop [i 0
                 ret (.asTransient (.-EMPTY PolyMap+-lookup_k))]
            (if (< i len)
              (recur (inc i) (-assoc! ret (aget arr i) (aget arr (inc i))))
              (-persistent! ret))))))

;;----------------------------------------------------------------------
;; Persistent Implementation
;;----------------------------------------------------------------------

(deftype PolyMap+-assoc_k_v|-lookup_k
         [^:IPersistentMap m
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
  (-invoke [this a b c d e f g h i j k l m n o p q r s t the-rest] (apply pm/handle-invoke this m impls metadata (concat [a b c d e f g h i j k l m n o p q r s t] the-rest)))
  ICollection
  (^PolyMap+-assoc_k_v|-lookup_k -conj [this entry]
    (if-let [f (get impls ::pm/-cons_v)]
      (f this m impls metadata entry)
      (PolyMap+-assoc_k_v|-lookup_k. (-conj m entry) impls metadata)))
  IEmptyableCollection
  (^PolyMap+-assoc_k_v|-lookup_k -empty [this]
    (if-let [f (get impls ::pm/-empty)]
      (f this m impls metadata)
      (PolyMap+-assoc_k_v|-lookup_k. (-empty m) impls metadata)))
  IMap
  (^PolyMap+-assoc_k_v|-lookup_k -dissoc [this k]
    (if-let [f (get impls ::pm/-without_k)]
      (f this m impls metadata k)
      (PolyMap+-assoc_k_v|-lookup_k. (-dissoc m k) impls metadata)))
  IAssociative
  (^PolyMap+-assoc_k_v|-lookup_k -assoc [this k v]
    ((get impls ::pm/-assoc_k_v) this m impls metadata k v))
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
  (^PolyMap+-assoc_k_v|-lookup_k -with-meta [this ^:IPersistentMap new-meta]
    (if-let [f (get impls ::pm/withMeta_meta)]
      (f this m impls metadata new-meta)
      (if (identical? new-meta metadata)
        this
        (PolyMap+-assoc_k_v|-lookup_k. m impls new-meta))))
  ICounted
  (^number -count [this]
    (if-let [f (get impls ::pm/count)]
      (f this m impls metadata)
      (-count m)))
  ILookup
  (-lookup [this k]
    ((get impls ::pm/-lookup_k) this m impls metadata k))
  (-lookup [this k nf]
    ((get impls ::pm/-lookup_k_nf) this m impls metadata k nf))
  IKVReduce
  (-kv-reduce [this f init]
    (if-let [reduce-fn (get impls ::pm/kv-reduce_f_init)]
      (reduce-fn this m impls metadata f init)
      (-kv-reduce m f init)))
  pm/IPolyAssociative
  (-assoc-impl [_this k v]
    (reproduce m (-assoc impls k v) metadata))
  (^boolean -contains-impl? [_this k]
    (-contains-key? impls k))
  (-impl [_this k]
    (-lookup impls k))
  (^IPersistentMap -get-impls [_this]
    impls)
  (-set-impls [_this ^:IPersistentMap new-impls]
    (reproduce m new-impls metadata))
  (^IPersistentMap -get-coll [_this]
    m)
  (-dissoc-impl [_this k]
    (reproduce m (-dissoc impls k) metadata))
  IEditableCollection
  (-as-transient [_this]
    (if (and (::tpm/-assoc!_k_v impls) (::tpm/-lookup_k impls))
      (TransientPolyMap+-assoc!_k_v|-lookup_k. true (transient m) impls metadata)
      (if (::tpm/-assoc!_k_v impls)
        (TransientPolyMap+-assoc!_k_v. true (transient m) impls metadata)
        (if (::tpm/-lookup_k impls)
          (TransientPolyMap+-lookup_k. true (transient m) impls metadata)
          (TransientPolyMap. true (transient m) impls metadata)))))
  IPrintWithWriter
  (-pr-writer [this writer opts]
    (let [m (.-m this)
          impls (.-impls this)
          metadata (.-metadata this)]
      (if-let [f (get impls ::pm/-pr-writer_writer_opts)]
        (f this m impls metadata writer opts)
        #_{:clj-kondo/ignore [:private-call]}
        (print-map this pr-writer writer opts)))))

;;----------------------------------------------------------------------
;; Static Methods / Setup for PolyMap
;;----------------------------------------------------------------------

(set! (.-EMPTY PolyMap+-assoc_k_v|-lookup_k) (PolyMap+-assoc_k_v|-lookup_k. {} {} {}))

(set! (.-fromArray PolyMap)
      (fn [arr ^boolean no-clone]
        (let [arr (if no-clone arr (.slice arr))
              len (.-length arr)]
          (loop [i 0
                 ret (.asTransient (.-EMPTY PolyMap+-assoc_k_v|-lookup_k))]
            (if (< i len)
              (recur (inc i) (-assoc! ret (aget arr i) (aget arr (inc i))))
              (-persistent! ret))))))

(defn reproduce [m impls metadata]
  (let [-assoc_k_v? (::pm/-assoc_k_v impls)
        -lookup_k? (::pm/-lookup_k impls)
        -lookup_k_nf? (::pm/-lookup_k_nf impls)]
    (if (and -assoc_k_v? (not -lookup_k?))
      (->PolyMap+-assoc_k_v m impls metadata)
      (if (and -lookup_k? -lookup_k_nf? (not -assoc_k_v?))
        (->PolyMap+-lookup_k m impls metadata)
        (if (and -assoc_k_v? -lookup_k?)
          (->PolyMap+-assoc_k_v|-lookup_k m impls metadata)
          (->PolyMap m impls metadata))))))

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
