(ns com.jolygon.wrap-map.api-0.impl
  "Internal implementation details for WrapMap (ClojureScript).
  Provides the WrapMap and TransientWrapMap deftypes and related protocols.
  Users should generally prefer the API functions in 'com.jolygon.wrap-map.api-0'
  or subsequent API versions, and use the implementation keys defined in
  'com.jolygon.wrap-map.api-0.common' and 'com.jolygon.wrap-map.api-0.trans.common'.

  This namespace is subject to change."
  (:require
    [com.jolygon.wrap-map.api-0.common :as wc]))

;;----------------------------------------------------------------------
;; Transient Implementation
;;----------------------------------------------------------------------

(declare WrapMap WrapMap+-assoc_k_v WrapMap+-lookup_k WrapMap+-assoc_k_v|-lookup_k
         ->WrapMap ->WrapMap+-assoc_k_v ->WrapMap+-lookup_k ->WrapMap+-assoc_k_v|-lookup_k
         ->TransientWrapMap ->TransientWrapMap+-assoc!_k_v ->TransientWrapMap+-lookup_k
         ->TransientWrapMap+-assoc!_k_v|-lookup_k construct)

(def allowable-impls
  #{:frozen? :metadata :this :wrap :twrap :pwrap :<-
    :toString :-conj_v :-empty :-dissoc_k :-assoc_k_v :-contains-key?_k
    :-find_k :-seq :-meta :withMeta_new-meta :-count :-lookup_k
    :-lookup_k_nf :kv-reduce_f_init :T_-conj! :T_-assoc!_k_v :T_-dissoc!_k
    :T_-lookup_k :T_-lookup_k_nf :T_-count :invoke :invoke-variadic
    :-pr-writer_writer_opts})

#_{:clj-kondo/ignore [:shadowed-var]}
(defrecord Impls
           [frozen? metadata this wrap twrap pwrap <-
            toString -conj_v -empty -dissoc_k -assoc_k_v -contains-key?_k -find_k
            -seq -meta withMeta_new-meta -count -lookup_k -lookup_k_nf kv-reduce_f_init
            T_-conj! T_-assoc!_k_v T_-dissoc!_k T_-lookup_k T_-lookup_k_nf T_-count
            invoke invoke-variadic -pr-writer_writer_opts])

(def empty-impls (map->Impls {}))

(deftype TransientWrapMap [^Impls e ^:mutable ^ITransientMap t_m]
  ITransientCollection
  (^TransientWrapMap -conj! [this entry]
    (if-let [f (.-T_conj_v e)]
      (do (set! t_m (f e t_m entry))
          this)
      (do (set! t_m (-conj! t_m entry))
          this)))
  (^WrapMap -persistent! [_this]
    ((.-pwrap e) e ^IPersistentMap (-persistent! t_m)))
  ITransientAssociative
  (^TransientWrapMap -assoc! [this k v]
    (set! t_m (-assoc! t_m k v))
    this)
  ITransientMap
  (^TransientWrapMap -dissoc! [this k]
    (if-let [f (.-T_-dissoc!_k e)]
      (do (set! t_m (f e t_m k))
          this)
      (do (set! t_m (-dissoc! t_m k))
          this)))
  ILookup
  (-lookup [_this k]
    (-lookup t_m k))
  (-lookup [_this k nf]
    (-lookup t_m k nf))
  ICounted
  (^number -count [_this]
    (if-let [f (.-T_count e)]
      (f e t_m)
      (-count t_m))))

(deftype TransientWrapMap+-assoc!_k_v [^Impls e ^:mutable ^ITransientMap t_m]
  ITransientCollection
  (^TransientWrapMap+-assoc!_k_v -conj! [this entry]
    (if-let [f (.-T_conj_v e)]
      (do (set! t_m (f e t_m entry))
          this)
      (do (set! t_m (-conj! t_m entry))
          this)))
  (^WrapMap -persistent! [_this]
    ((.-pwrap e) e ^IPersistentMap (-persistent! t_m)))
  ITransientAssociative
  (^TransientWrapMap+-assoc!_k_v -assoc! [this k v]
    (set! t_m ((.-T_-assoc!_k_v e) e t_m k v))
    this)
  ITransientMap
  (^TransientWrapMap+-assoc!_k_v -dissoc! [this k]
    (if-let [f (.-T_-dissoc!_k e)]
      (do (set! t_m (f e t_m k))
          this)
      (do (set! t_m (-dissoc! t_m k))
          this)))
  ILookup
  (-lookup [_this k]
    (-lookup t_m k))
  (-lookup [_this k nf]
    (-lookup t_m k nf))
  ICounted
  (^number -count [_this]
    (if-let [f (.-T_count e)]
      (f e t_m)
      (-count t_m))))

(deftype TransientWrapMap+-lookup_k [^Impls e ^:mutable ^ITransientMap t_m]
  ITransientCollection
  (^TransientWrapMap+-lookup_k -conj! [this entry]
    (if-let [f (.-T_conj_v e)]
      (do (set! t_m (f e t_m entry))
          this)
      (do (set! t_m (-conj! t_m entry))
          this)))
  (^WrapMap -persistent! [_this]
    ((.-pwrap e) e ^IPersistentMap (-persistent! t_m)))
  ITransientAssociative
  (^TransientWrapMap+-lookup_k -assoc! [this k v]
    (set! t_m (-assoc! t_m k v))
    this)
  ITransientMap
  (^TransientWrapMap+-lookup_k -dissoc! [this k]
    (if-let [f (.-T_-dissoc!_k e)]
      (do (set! t_m (f e t_m k))
          this)
      (do (set! t_m (-dissoc! t_m k))
          this)))
  ILookup
  (-lookup [_this k]
    ((.-T_-lookup_k e) e t_m k))
  (-lookup [_this k nf]
    ((.-T_-lookup_k_nf e) e t_m k nf))
  ICounted
  (^number -count [_this]
    (if-let [f (.-T_count e)]
      (f e t_m)
      (-count t_m))))

(deftype TransientWrapMap+-assoc!_k_v|-lookup_k [^Impls e ^:mutable ^ITransientMap t_m]
  ITransientCollection
  (^TransientWrapMap+-assoc!_k_v|-lookup_k -conj! [this entry]
    (if-let [f (.-T_conj_v e)]
      (do (set! t_m (f e t_m entry))
          this)
      (do (set! t_m (-conj! t_m entry))
          this)))
  (^WrapMap -persistent! [_this]
    ((.-pwrap e) e ^IPersistentMap (-persistent! t_m)))
  ITransientAssociative
  (^TransientWrapMap+-assoc!_k_v|-lookup_k -assoc! [this k v]
    (set! t_m ((.-T_-assoc!_k_v e) e t_m k v))
    this)
  ITransientMap
  (^TransientWrapMap+-assoc!_k_v|-lookup_k -dissoc! [this k]
    (if-let [f (.-T_-dissoc!_k e)]
      (do (set! t_m (f e t_m k))
          this)
      (do (set! t_m (-dissoc! t_m k))
          this)))
  ILookup
  (-lookup [_this k]
    ((.-T_-lookup_k e) e t_m k))
  (-lookup [_this k nf]
    ((.-T_-lookup_k_nf e) e t_m k nf))
  ICounted
  (^number -count [_this]
    (if-let [f (.-T_count e)]
      (f e t_m)
      (-count t_m))))

;;----------------------------------------------------------------------
;; Persistent Implementation
;;----------------------------------------------------------------------

(deftype WrapMap [^:Impls e ^:IPersistentMap m]
  Object
  (toString [_this]
    (if-let [f (.-toString e)]
      (f e m)
      (pr-str* m)))
  IHash
  (-hash [_this]
    (-hash m))
  IEquiv
  (-equiv [_this other]
    (-equiv m other))
  IFn
  (-invoke [_this] (apply wc/handle-invoke e m []))
  (-invoke [_this k] (apply wc/handle-invoke e m [k]))
  (-invoke [_this k nf] (apply wc/handle-invoke e m [k nf]))
  (-invoke [_this a b c] (apply wc/handle-invoke e m [a b c]))
  (-invoke [_this a b c d] (apply wc/handle-invoke e m [a b c d]))
  (-invoke [_this a b c d e] (apply wc/handle-invoke e m [a b c d e]))
  (-invoke [_this a b c d e f] (apply wc/handle-invoke e m [a b c d e f]))
  (-invoke [_this a b c d e f g] (apply wc/handle-invoke e m [a b c d e f g]))
  (-invoke [_this a b c d e f g h] (apply wc/handle-invoke e m [a b c d e f g h]))
  (-invoke [_this a b c d e f g h i] (apply wc/handle-invoke e m [a b c d e f g h i]))
  (-invoke [_this a b c d e f g h i j] (apply wc/handle-invoke e m [a b c d e f g h i j]))
  (-invoke [_this a b c d e f g h i j k] (apply wc/handle-invoke e m [a b c d e f g h i j k]))
  (-invoke [_this a b c d e f g h i j k l] (apply wc/handle-invoke e m [a b c d e f g h i j k l]))
  (-invoke [_this a b c d e f g h i j k l m] (apply wc/handle-invoke e m [a b c d e f g h i j k l m]))
  (-invoke [_this a b c d e f g h i j k l m n] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n]))
  (-invoke [_this a b c d e f g h i j k l m n o] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o]))
  (-invoke [_this a b c d e f g h i j k l m n o p] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p]))
  (-invoke [_this a b c d e f g h i j k l m n o p q] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r s]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s t] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r s t]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s t the-rest] (apply wc/handle-invoke e m (concat [a b c d e f g h i j k l m n o p q r s t] the-rest)))
  ICollection
  (^WrapMap -conj [_this entry]
    (if-let [f (.--conj_v e)]
      (f e m entry)
      (WrapMap. e (-conj m entry))))
  IEmptyableCollection
  (^WrapMap -empty [_this]
    (if-let [f (.--empty e)]
      (f e m)
      (WrapMap. e (-empty m))))
  IMap
  (^WrapMap -dissoc [_this k]
    (if-let [f (.--dissoc_k e)]
      (f e m k)
      (WrapMap. e (-dissoc m k))))

  IAssociative
  (^WrapMap -assoc [_this k v] (WrapMap. e (-assoc m k v)))

  (^boolean -contains-key? [_this k]
    (if-let [f (.--contains-key?_k e)]
      (f e m k)
      (-contains-key? m k)))
  IFind
  (-find [_this k]
    (if-let [f (.--find_k e)]
      (f e m k)
      (-find m k)))
  ISeqable
  (-seq [_this]
    (if-let [f (.--seq e)]
      (f e m)
      (-seq m)))
  IIterable
  IMeta
  (-meta [_this]
    (if-let [f (.--meta e)]
      (f e m)
      (.-metadata e)))
  IWithMeta
  (^WrapMap -with-meta [this ^:IPersistentMap new-meta]
    (if-let [f (.-withMeta_meta e)]
      (f e m new-meta)
      (if (identical? new-meta (.-metadata e))
        this
        (WrapMap. (assoc e :metadata new-meta) (with-meta m new-meta)))))
  ICounted
  (^number -count [_this]
    (if-let [f (.-count e)]
      (f e m)
      (-count m)))

  ILookup
  (-lookup [_this k] (-lookup m k))
  (-lookup [_this k nf] (-lookup m k nf))

  IKVReduce
  (-kv-reduce [_this f init]
    (if-let [reduce-fn (.-kv-reduce_f_init e)]
      (reduce-fn e m f init)
      (-kv-reduce m f init)))
  wc/IWrapAssociative
  (-assoc-impl [_this k v]
    (assert (allowable-impls k))
    (if (.-frozen? e)
      (throw (ex-info "Cannot associate impl on frozen wrap map" {}))
      (construct (map->Impls (assoc e k v)) m)))
  (^boolean -contains-impl? [_this k]
    (not (nil? (get e k))))
  (-impl [_this k]
    (-lookup e k))
  (^IPersistentMap -get-impls [_this]
    e)
  (-with-wrap [_this ^:IPersistentMap new-impls]
    (assert (every? allowable-impls (keys new-impls)))
    (if (.-frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls new-impls) m)))
  (-vary [_this afn args]
    (if (.-frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (let [new-impls (apply afn e args)]
        (assert (every? allowable-impls (keys new-impls)))
        (construct (map->Impls new-impls) m))))
  (^IPersistentMap -unwrap [_this]
    m)
  (-dissoc-impl [_this k]
    (assert (allowable-impls k))
    (if (.-frozen? e)
      (throw (ex-info "Cannot disassociate impl on frozen wrap map" {}))
      (construct (-dissoc e k) m)))
  (-freeze [_this]
    (WrapMap. (assoc e :frozen? true) m))
  IEditableCollection
  (-as-transient [_this]
    ((.-twrap e) e ^IPersistentMap (transient m)))
  IPrintWithWriter
  (-pr-writer [this writer opts]
    (let [m (.-m this)
          e (.-e this)]
      (if-let [f (.--pr-writer_writer_opts e)]
        (f e m writer opts)
        #_{:clj-kondo/ignore [:private-call]}
        (print-map this pr-writer writer opts)))))

;;----------------------------------------------------------------------
;; Static Methods / Setup for WrapMap
;;----------------------------------------------------------------------

(set! (.-EMPTY WrapMap) (WrapMap. empty-impls {}))

(set! (.-fromArray WrapMap)
      (fn [arr ^boolean no-clone]
        (let [arr (if no-clone arr (.slice arr))
              len (.-length arr)]
          (loop [i 0
                 ret (.asTransient (.-EMPTY WrapMap))]
            (if (< i len)
              (recur (inc i) (-assoc! ret (aget arr i) (aget arr (inc i))))
              (-persistent! ret))))))

;;----------------------------------------------------------------------
;; Persistent Implementation
;;----------------------------------------------------------------------

(deftype WrapMap+-assoc_k_v [^:Impls e ^:IPersistentMap m]
  Object
  (toString [_this]
    (if-let [f (.-toString e)]
      (f e m)
      (pr-str* m)))
  IHash
  (-hash [_this]
    (-hash m))
  IEquiv
  (-equiv [_this other]
    (-equiv m other))
  IFn
  (-invoke [_this] (apply wc/handle-invoke e m []))
  (-invoke [_this k] (apply wc/handle-invoke e m [k]))
  (-invoke [_this k nf] (apply wc/handle-invoke e m [k nf]))
  (-invoke [_this a b c] (apply wc/handle-invoke e m [a b c]))
  (-invoke [_this a b c d] (apply wc/handle-invoke e m [a b c d]))
  (-invoke [_this a b c d e] (apply wc/handle-invoke e m [a b c d e]))
  (-invoke [_this a b c d e f] (apply wc/handle-invoke e m [a b c d e f]))
  (-invoke [_this a b c d e f g] (apply wc/handle-invoke e m [a b c d e f g]))
  (-invoke [_this a b c d e f g h] (apply wc/handle-invoke e m [a b c d e f g h]))
  (-invoke [_this a b c d e f g h i] (apply wc/handle-invoke e m [a b c d e f g h i]))
  (-invoke [_this a b c d e f g h i j] (apply wc/handle-invoke e m [a b c d e f g h i j]))
  (-invoke [_this a b c d e f g h i j k] (apply wc/handle-invoke e m [a b c d e f g h i j k]))
  (-invoke [_this a b c d e f g h i j k l] (apply wc/handle-invoke e m [a b c d e f g h i j k l]))
  (-invoke [_this a b c d e f g h i j k l m] (apply wc/handle-invoke e m [a b c d e f g h i j k l m]))
  (-invoke [_this a b c d e f g h i j k l m n] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n]))
  (-invoke [_this a b c d e f g h i j k l m n o] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o]))
  (-invoke [_this a b c d e f g h i j k l m n o p] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p]))
  (-invoke [_this a b c d e f g h i j k l m n o p q] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r s]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s t] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r s t]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s t the-rest] (apply wc/handle-invoke e m (concat [a b c d e f g h i j k l m n o p q r s t] the-rest)))
  ICollection
  (^WrapMap+-assoc_k_v -conj [_this entry]
    (if-let [f (.--conj_v e)]
      (f e m entry)
      (WrapMap+-assoc_k_v. e (-conj m entry))))
  IEmptyableCollection
  (^WrapMap+-assoc_k_v -empty [_this]
    (if-let [f (.--empty e)]
      (f e m)
      (WrapMap+-assoc_k_v. e (-empty m))))
  IMap
  (^WrapMap+-assoc_k_v -dissoc [_this k]
    (if-let [f (.--dissoc_k e)]
      (f e m k)
      (WrapMap+-assoc_k_v. e (-dissoc m k))))

  IAssociative
  (^WrapMap+-assoc_k_v -assoc [_this k v]
    ((.--assoc_k_v e) e m k v))

  (^boolean -contains-key? [_this k]
    (if-let [f (.--contains-key?_k e)]
      (f e m k)
      (-contains-key? m k)))
  IFind
  (-find [_this k]
    (if-let [f (.--find_k e)]
      (f e m k)
      (-find m k)))
  ISeqable
  (-seq [_this]
    (if-let [f (.--seq e)]
      (f e m)
      (-seq m)))
  IIterable
  IMeta
  (-meta [_this]
    (if-let [f (.--meta e)]
      (f e m)
      (.-metadata e)))
  IWithMeta
  (^WrapMap+-assoc_k_v -with-meta [this ^:IPersistentMap new-meta]
    (if-let [f (.-withMeta_meta e)]
      (f e m new-meta)
      (if (identical? new-meta (.-metadata e))
        this
        (WrapMap+-assoc_k_v. (assoc e :metadata new-meta) (with-meta m new-meta)))))
  ICounted
  (^number -count [_this]
    (if-let [f (.-count e)]
      (f e m)
      (-count m)))
  ILookup
  (-lookup [_this k]
    (-lookup m k))
  (-lookup [_this k nf]
    (-lookup m k nf))
  IKVReduce
  (-kv-reduce [_this f init]
    (if-let [reduce-fn (.-kv-reduce_f_init e)]
      (reduce-fn e m f init)
      (-kv-reduce m f init)))
  wc/IWrapAssociative
  (-assoc-impl [_this k v]
    (assert (allowable-impls k))
    (if (.-frozen? e)
      (throw (ex-info "Cannot associate impl on frozen wrap map" {}))
      (construct (map->Impls (assoc e k v)) m)))
  (^boolean -contains-impl? [_this k]
    (not (nil? (get e k))))
  (-impl [_this k]
    (-lookup e k))
  (^IPersistentMap -get-impls [_this]
    e)
  (-with-wrap [_this ^:IPersistentMap new-impls]
    (assert (every? allowable-impls (keys new-impls)))
    (if (.-frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls new-impls) m)))
  (-vary [_this afn args]
    (if (.-frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (let [new-impls (apply afn e args)]
        (assert (every? allowable-impls (keys new-impls)))
        (construct (map->Impls new-impls) m))))
  (^IPersistentMap -unwrap [_this]
    m)
  (-dissoc-impl [_this k]
    (assert (allowable-impls k))
    (if (.-frozen? e)
      (throw (ex-info "Cannot disassociate impl on frozen wrap map" {}))
      (construct (-dissoc e k) m)))
  (-freeze [_this]
    (WrapMap+-assoc_k_v. (assoc e :frozen? true) m))
  IEditableCollection
  (-as-transient [_this]
    ((.-twrap e) e ^IPersistentMap (transient m)))
  IPrintWithWriter
  (-pr-writer [this writer opts]
    (let [m (.-m this)
          e (.-e this)]
      (if-let [f (.--pr-writer_writer_opts e)]
        (f e m writer opts)
        #_{:clj-kondo/ignore [:private-call]}
        (print-map this pr-writer writer opts)))))

;;----------------------------------------------------------------------
;; Static Methods / Setup for WrapMap
;;----------------------------------------------------------------------

(set! (.-EMPTY WrapMap+-assoc_k_v) (WrapMap+-assoc_k_v. empty-impls {}))

(set! (.-fromArray WrapMap+-assoc_k_v)
      (fn [arr ^boolean no-clone]
        (let [arr (if no-clone arr (.slice arr))
              len (.-length arr)]
          (loop [i 0
                 ret (.asTransient (.-EMPTY WrapMap+-assoc_k_v))]
            (if (< i len)
              (recur (inc i) (-assoc! ret (aget arr i) (aget arr (inc i))))
              (-persistent! ret))))))

;;----------------------------------------------------------------------
;; Persistent Implementation
;;----------------------------------------------------------------------

(deftype WrapMap+-lookup_k [^:Impls e ^:IPersistentMap m]
  Object
  (toString [_this]
    (if-let [f (.-toString e)]
      (f e m)
      (pr-str* m)))
  IHash
  (-hash [_this]
    (-hash m))
  IEquiv
  (-equiv [_this other]
    (-equiv m other))
  IFn
  (-invoke [_this] (apply wc/handle-invoke e m []))
  (-invoke [_this k] (apply wc/handle-invoke e m [k]))
  (-invoke [_this k nf] (apply wc/handle-invoke e m [k nf]))
  (-invoke [_this a b c] (apply wc/handle-invoke e m [a b c]))
  (-invoke [_this a b c d] (apply wc/handle-invoke e m [a b c d]))
  (-invoke [_this a b c d e] (apply wc/handle-invoke e m [a b c d e]))
  (-invoke [_this a b c d e f] (apply wc/handle-invoke e m [a b c d e f]))
  (-invoke [_this a b c d e f g] (apply wc/handle-invoke e m [a b c d e f g]))
  (-invoke [_this a b c d e f g h] (apply wc/handle-invoke e m [a b c d e f g h]))
  (-invoke [_this a b c d e f g h i] (apply wc/handle-invoke e m [a b c d e f g h i]))
  (-invoke [_this a b c d e f g h i j] (apply wc/handle-invoke e m [a b c d e f g h i j]))
  (-invoke [_this a b c d e f g h i j k] (apply wc/handle-invoke e m [a b c d e f g h i j k]))
  (-invoke [_this a b c d e f g h i j k l] (apply wc/handle-invoke e m [a b c d e f g h i j k l]))
  (-invoke [_this a b c d e f g h i j k l m] (apply wc/handle-invoke e m [a b c d e f g h i j k l m]))
  (-invoke [_this a b c d e f g h i j k l m n] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n]))
  (-invoke [_this a b c d e f g h i j k l m n o] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o]))
  (-invoke [_this a b c d e f g h i j k l m n o p] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p]))
  (-invoke [_this a b c d e f g h i j k l m n o p q] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r s]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s t] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r s t]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s t the-rest] (apply wc/handle-invoke e m (concat [a b c d e f g h i j k l m n o p q r s t] the-rest)))
  ICollection
  (^WrapMap+-lookup_k -conj [_this entry]
    (if-let [f (.--conj_v e)]
      (f e m entry)
      (WrapMap+-lookup_k. e (-conj m entry))))
  IEmptyableCollection
  (^WrapMap+-lookup_k -empty [_this]
    (if-let [f (.--empty e)]
      (f e m)
      (WrapMap+-lookup_k. e (-empty m))))
  IMap
  (^WrapMap+-lookup_k -dissoc [_this k]
    (if-let [f (.--dissoc_k e)]
      (f e m k)
      (WrapMap+-lookup_k. e (-dissoc m k))))
  IAssociative
  (^WrapMap+-lookup_k -assoc [_this k v]
    (WrapMap+-lookup_k. e (-assoc m k v)))
  (^boolean -contains-key? [_this k]
    (if-let [f (.--contains-key?_k e)]
      (f e m k)
      (-contains-key? m k)))
  IFind
  (-find [_this k]
    (if-let [f (.--find_k e)]
      (f e m k)
      (-find m k)))
  ISeqable
  (-seq [_this]
    (if-let [f (.--seq e)]
      (f e m)
      (-seq m)))
  IIterable
  IMeta
  (-meta [_this]
    (if-let [f (.--meta e)]
      (f e m)
      (.-metadata e)))
  IWithMeta
  (^WrapMap+-lookup_k -with-meta [this ^:IPersistentMap new-meta]
    (if-let [f (.-withMeta_meta e)]
      (f e m new-meta)
      (if (identical? new-meta (.-metadata e))
        this
        (WrapMap+-lookup_k. (assoc e :metadata new-meta) (with-meta m new-meta)))))
  ICounted
  (^number -count [_this]
    (if-let [f (.-count e)]
      (f e m)
      (-count m)))
  ILookup
  (-lookup [_this k]
    ((.--lookup_k e) e m k))
  (-lookup [_this k nf]
    ((.--lookup_k_nf e) e m k nf))
  IKVReduce
  (-kv-reduce [_this f init]
    (if-let [reduce-fn (.-kv-reduce_f_init e)]
      (reduce-fn e m f init)
      (-kv-reduce m f init)))
  wc/IWrapAssociative
  (-assoc-impl [_this k v]
    (assert (allowable-impls k))
    (if (.-frozen? e)
      (throw (ex-info "Cannot associate impl on frozen wrap map" {}))
      (construct (map->Impls (assoc e k v)) m)))
  (^boolean -contains-impl? [_this k]
    (not (nil? (get e k))))
  (-impl [_this k]
    (-lookup e k))
  (^IPersistentMap -get-impls [_this]
    e)
  (-with-wrap [_this ^:IPersistentMap new-impls]
    (assert (every? allowable-impls (keys new-impls)))
    (if (.-frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls new-impls) m)))
  (-vary [_this afn args]
    (if (.-frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (let [new-impls (apply afn e args)]
        (assert (every? allowable-impls (keys new-impls)))
        (construct (map->Impls new-impls) m))))
  (^IPersistentMap -unwrap [_this]
    m)
  (-dissoc-impl [_this k]
    (assert (allowable-impls k))
    (if (.-frozen? e)
      (throw (ex-info "Cannot disassociate impl on frozen wrap map" {}))
      (construct (-dissoc e k) m)))
  (-freeze [_this]
    (WrapMap+-lookup_k. (assoc e :frozen? true) m))
  IEditableCollection
  (-as-transient [_this]
    ((.-twrap e) e ^IPersistentMap (transient m)))
  IPrintWithWriter
  (-pr-writer [this writer opts]
    (let [m (.-m this)
          e (.-e this)]
      (if-let [f (.--pr-writer_writer_opts e)]
        (f e m writer opts)
        #_{:clj-kondo/ignore [:private-call]}
        (print-map this pr-writer writer opts)))))

;;----------------------------------------------------------------------
;; Static Methods / Setup for WrapMap
;;----------------------------------------------------------------------

(set! (.-EMPTY WrapMap) (WrapMap+-lookup_k. empty-impls {}))

(set! (.-fromArray WrapMap+-lookup_k)
      (fn [arr ^boolean no-clone]
        (let [arr (if no-clone arr (.slice arr))
              len (.-length arr)]
          (loop [i 0
                 ret (.asTransient (.-EMPTY WrapMap+-lookup_k))]
            (if (< i len)
              (recur (inc i) (-assoc! ret (aget arr i) (aget arr (inc i))))
              (-persistent! ret))))))

;;----------------------------------------------------------------------
;; Persistent Implementation
;;----------------------------------------------------------------------

(deftype WrapMap+-assoc_k_v|-lookup_k [^:Impls e ^:IPersistentMap m]
  Object
  (toString [_this]
    (if-let [f (.-toString e)]
      (f e m)
      (pr-str* m)))
  IHash
  (-hash [_this]
    (-hash m))
  IEquiv
  (-equiv [_this other]
    (-equiv m other))
  IFn
  (-invoke [_this] (apply wc/handle-invoke e m []))
  (-invoke [_this k] (apply wc/handle-invoke e m [k]))
  (-invoke [_this k nf] (apply wc/handle-invoke e m [k nf]))
  (-invoke [_this a b c] (apply wc/handle-invoke e m [a b c]))
  (-invoke [_this a b c d] (apply wc/handle-invoke e m [a b c d]))
  (-invoke [_this a b c d e] (apply wc/handle-invoke e m [a b c d e]))
  (-invoke [_this a b c d e f] (apply wc/handle-invoke e m [a b c d e f]))
  (-invoke [_this a b c d e f g] (apply wc/handle-invoke e m [a b c d e f g]))
  (-invoke [_this a b c d e f g h] (apply wc/handle-invoke e m [a b c d e f g h]))
  (-invoke [_this a b c d e f g h i] (apply wc/handle-invoke e m [a b c d e f g h i]))
  (-invoke [_this a b c d e f g h i j] (apply wc/handle-invoke e m [a b c d e f g h i j]))
  (-invoke [_this a b c d e f g h i j k] (apply wc/handle-invoke e m [a b c d e f g h i j k]))
  (-invoke [_this a b c d e f g h i j k l] (apply wc/handle-invoke e m [a b c d e f g h i j k l]))
  (-invoke [_this a b c d e f g h i j k l m] (apply wc/handle-invoke e m [a b c d e f g h i j k l m]))
  (-invoke [_this a b c d e f g h i j k l m n] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n]))
  (-invoke [_this a b c d e f g h i j k l m n o] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o]))
  (-invoke [_this a b c d e f g h i j k l m n o p] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p]))
  (-invoke [_this a b c d e f g h i j k l m n o p q] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r s]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s t] (apply wc/handle-invoke e m [a b c d e f g h i j k l m n o p q r s t]))
  (-invoke [_this a b c d e f g h i j k l m n o p q r s t the-rest] (apply wc/handle-invoke e m (concat [a b c d e f g h i j k l m n o p q r s t] the-rest)))
  ICollection
  (^WrapMap+-assoc_k_v|-lookup_k -conj [_this entry]
    (if-let [f (.--conj_v e)]
      (f e m entry)
      (WrapMap+-assoc_k_v|-lookup_k. e (-conj m entry))))
  IEmptyableCollection
  (^WrapMap+-assoc_k_v|-lookup_k -empty [_this]
    (if-let [f (.--empty e)]
      (f e m)
      (WrapMap+-assoc_k_v|-lookup_k. e (-empty m))))
  IMap
  (^WrapMap+-assoc_k_v|-lookup_k -dissoc [_this k]
    (if-let [f (.--dissoc_k e)]
      (f e m k)
      (WrapMap+-assoc_k_v|-lookup_k. e (-dissoc m k))))
  IAssociative
  (^WrapMap+-assoc_k_v|-lookup_k -assoc [_this k v]
    ((.--assoc_k_v e) e m k v))
  (^boolean -contains-key? [_this k]
    (if-let [f (.--contains-key?_k e)]
      (f e m k)
      (-contains-key? m k)))
  IFind
  (-find [_this k]
    (if-let [f (.--find_k e)]
      (f e m k)
      (-find m k)))
  ISeqable
  (-seq [_this]
    (if-let [f (.--seq e)]
      (f e m)
      (-seq m)))
  IIterable
  IMeta
  (-meta [_this]
    (if-let [f (.--meta e)]
      (f e m)
      (.-metadata e)))
  IWithMeta
  (^WrapMap+-assoc_k_v|-lookup_k -with-meta [this ^:IPersistentMap new-meta]
    (if-let [f (.-withMeta_meta e)]
      (f e m new-meta)
      (if (identical? new-meta (.-metadata e))
        this
        (WrapMap+-assoc_k_v|-lookup_k. (assoc e :metadata new-meta) (with-meta m new-meta)))))
  ICounted
  (^number -count [_this]
    (if-let [f (.-count e)]
      (f e m)
      (-count m)))
  ILookup
  (-lookup [_this k]
    ((.--lookup_k e) e m k))
  (-lookup [_this k nf]
    ((.--lookup_k_nf e) e m k nf))
  IKVReduce
  (-kv-reduce [_this f init]
    (if-let [reduce-fn (.-kv-reduce_f_init e)]
      (reduce-fn e m f init)
      (-kv-reduce m f init)))
  wc/IWrapAssociative
  (-assoc-impl [_this k v]
    (assert (allowable-impls k))
    (if (.-frozen? e)
      (throw (ex-info "Cannot associate impl on frozen wrap map" {}))
      (construct (map->Impls (assoc e k v)) m)))
  (^boolean -contains-impl? [_this k]
    (not (nil? (get e k))))
  (-impl [_this k]
    (-lookup e k))
  (^IPersistentMap -get-impls [_this]
    e)
  (-with-wrap [_this ^:IPersistentMap new-impls]
    (assert (every? allowable-impls (keys new-impls)))
    (if (.-frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (construct (map->Impls new-impls) m)))
  (-vary [_this afn args]
    (if (.-frozen? e)
      (throw (ex-info "Cannot set impls on frozen wrap map" {}))
      (let [new-impls (apply afn e args)]
        (assert (every? allowable-impls (keys new-impls)))
        (construct (map->Impls new-impls) m))))
  (^IPersistentMap -unwrap [_this]
    m)
  (-dissoc-impl [_this k]
    (assert (allowable-impls k))
    (if (.-frozen? e)
      (throw (ex-info "Cannot disassociate impl on frozen wrap map" {}))
      (construct (-dissoc e k) m)))
  (-freeze [_this]
    (WrapMap+-assoc_k_v|-lookup_k. (assoc e :frozen? true) m))
  IEditableCollection
  (-as-transient [_this]
    ((.-twrap e) e ^IPersistentMap (transient m)))
  IPrintWithWriter
  (-pr-writer [this writer opts]
    (let [m (.-m this)
          e (.-e this)]
      (if-let [f (.--pr-writer_writer_opts e)]
        (f e m writer opts)
        #_{:clj-kondo/ignore [:private-call]}
        (print-map this pr-writer writer opts)))))

;;----------------------------------------------------------------------
;; Static Methods / Setup for WrapMap
;;----------------------------------------------------------------------

(set! (.-EMPTY WrapMap+-assoc_k_v|-lookup_k) (WrapMap+-assoc_k_v|-lookup_k. empty-impls {}))

(set! (.-fromArray WrapMap)
      (fn [arr ^boolean no-clone]
        (let [arr (if no-clone arr (.slice arr))
              len (.-length arr)]
          (loop [i 0
                 ret (.asTransient (.-EMPTY WrapMap+-assoc_k_v|-lookup_k))]
            (if (< i len)
              (recur (inc i) (-assoc! ret (aget arr i) (aget arr (inc i))))
              (-persistent! ret))))))

(defn get-wrap-persistent [e]
  (let [-assoc_k_v? (.--assoc_k_v e)
        -lookup_k? (.--lookup_k e)
        -lookup_k_nf? (.--lookup_k_nf e)]
    (if (and -assoc_k_v? (not -lookup_k?))
      ->WrapMap+-assoc_k_v
      (if (and -lookup_k? -lookup_k_nf? (not -assoc_k_v?))
        ->WrapMap+-lookup_k
        (if (and -assoc_k_v? -lookup_k?)
          ->WrapMap+-assoc_k_v|-lookup_k
          ->WrapMap)))))

(defn get-wrap-transient [e]
  (if (and (.-T_-assoc!_k_v e) (.-T_-lookup_k e))
    ->TransientWrapMap+-assoc!_k_v|-lookup_k
    (if (.-T_-assoc!_k_v e)
      ->TransientWrapMap+-assoc!_k_v
      (if (.-T_-lookup_k e)
        ->TransientWrapMap+-lookup_k
        ->TransientWrapMap))))

(defn construct [e m & [transient?]]
  (if-not (instance? Impls e)
    (construct (map->Impls e) m)
    (let [pwrap (get-wrap-persistent e)
          twrap (get-wrap-transient e)
          new-impls (assoc e :<- pwrap :pwrap pwrap :twrap twrap :metadata (meta m))]
      (if transient?
        (twrap new-impls m)
        (pwrap new-impls m)))))

;;----------------------------------------------------------------------
;; Constructor
;;----------------------------------------------------------------------

(defn wrap-map*
  "Internal raw constructor for WrapMap. Creates a WrapMap instance
  directly from the underlying collection `m` and environment map
  `e`. Does NOT perform the internal preparation step (like
  ensuring default-invoke). Prefer `com.jolygon.wrap-map.api-0/wrap`
  for general use."
  ^WrapMap
  ([] (construct empty-impls {}))
  ^WrapMap
  ([^IPersistentMap m] (construct empty-impls m))
  ^WrapMap
  ([^Impls e ^IPersistentMap m] (construct e m)))
