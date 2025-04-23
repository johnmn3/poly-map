(ns com.jolygon.wrap-map.api-0
  (:refer-clojure :exclude [assoc dissoc])
  (:require
    [com.jolygon.wrap-map.api-0.common :as wc]
    [com.jolygon.wrap-map.api-0.impl :as mi]))

(defn make-wrap
  "Internal raw constructor for WrapMap. Creates a WrapMap instance
  directly from the underlying collection `m` and environment map
  `e`. Does NOT perform the internal preparation step (like
  ensuring default-invoke). Prefer `com.jolygon.wrap-map/wrap`
  for general use."
  ([] (make-wrap {} {}))
  ([m]
   (make-wrap {} m))
  ([e m]
   (mi/wrap-map* (mi/map->Impls e) m)))

(def empty-wrap (make-wrap))

(defn wrap
  "keyval => key val
  Returns a new wrap-map with supplied mappings.  If any keys are
  equal, they are handled as if by repeated uses of assoc."
  [& kvs]
  (if (= 1 (count kvs))
    (make-wrap (first kvs))
    (make-wrap (apply hash-map kvs))))

(defn freeze
  [coll]
  (wc/-freeze coll))

(defn contains-impl?
  [coll k]
  (wc/-contains-impl? coll k))

(defn get-impl
  [coll k]
  (wc/-impl coll k))

(defn get-impls
  [coll]
  (wc/-get-impls coll))

(defn with-wrap
  [coll new-impls]
  (if-not (satisfies? wc/IWrapAssociative coll)
    (with-wrap (wrap coll) new-impls)
    (wc/-with-wrap coll new-impls)))

(defn vary
  [coll afn & args]
  (if-not (satisfies? wc/IWrapAssociative coll)
    (apply vary (wrap coll) afn args)
    (wc/-vary coll afn args)))

(defn unwrap
  [coll]
  (wc/-unwrap coll))

(defn dissoc-impl
  [coll & ks]
  (if-not (satisfies? wc/IWrapAssociative coll) ;; <- should never actually be true
    (apply dissoc-impl (wrap coll) ks)
    (->> ks
         (reduce (fn [c k]
                   (wc/-dissoc-impl c k))
                 coll))))

(defn assoc-impl
  [coll & kvs]
  (if-not (satisfies? wc/IWrapAssociative coll)
    (apply assoc-impl (wrap coll) kvs)
    (->> kvs
         (partition 2)
         (reduce (fn [c [k v]]
                   (wc/-assoc-impl c k v))
                 coll))))

;; High Level API

(defn p-assoc [pm behavior-key handler-fn]
  (case behavior-key
    :get
    (let [internal-get (fn [_e m k] (handler-fn m k))
          internal-get-nf (fn [_e m k nf] (handler-fn m k nf))]
      (assoc-impl pm
                  :invoke-variadic (fn invoke-variadic
                                     ([_e m k]
                                      (handler-fn m k))
                                     ([_e m k nf]
                                      (handler-fn m k nf)))
                  #?@(:clj [:valAt_k internal-get
                            :valAt_k_nf internal-get-nf
                            :T_valAt_k internal-get               ;; <- get can handle both persistent and transient
                            :T_valAt_k_nf internal-get-nf]
                      :cljs [:-lookup_k internal-get
                             :-lookup_k_nf internal-get-nf
                             :T-lookup_k internal-get
                             :T-lookup_k_nf internal-get-nf])))
    :assoc
    (let [internal-assoc (fn [e m k v]
                           (let [new-m (handler-fn m k v)]
                             (make-wrap e new-m)))]
      (assoc-impl pm
                  #?@(:clj [:assoc_k_v internal-assoc]
                      :cljs [:-assoc_k_v internal-assoc])))
    :dissoc
    (let [internal-dissoc (fn [e m k]
                            (let [new-m (handler-fn m k)]
                              (make-wrap e new-m)))]
      (assoc-impl pm
                  #?@(:clj [:without_k internal-dissoc]
                      :cljs [:-dissoc_k internal-dissoc])))
    :contains?
    (let [internal-contains (fn [_e m k] (handler-fn m k))]
      (assoc-impl pm
                  #?@(:clj [:containsKey_k internal-contains]
                      :cljs [:-contains-key?_k internal-contains])))
    :invoke
    (let [internal-invoke (fn [_e m & args] (apply handler-fn m args))]
      (assoc-impl pm :invoke-variadic internal-invoke))

    :print
    (let [internal-to-string (fn [_e m]
                               (handler-fn m))
          internal-pr-writer (fn [_e m writer & [_opts]]
                               (let [s ^String (handler-fn m)]
                                 #?(:clj (.write ^java.io.Writer writer s)
                                    :cljs (cljs.core/-write writer s))))]
      (assoc-impl pm
                  :toString internal-to-string
                  #?@(:clj [:print-method_writer internal-pr-writer]
                      :cljs [:-pr-writer_writer_opts internal-pr-writer])))
    ;; :transient
    :assoc!
    (let [T_internal-assoc (fn [_e m k v]
                             (handler-fn m k v))]
      (assoc-impl pm
                  #?@(:clj [:T_assoc_k_v T_internal-assoc]
                      :cljs [:T_-assoc_k_v T_internal-assoc])))

    :dissoc!
    (let [T_internal-dissoc (fn [_e m k]
                              (handler-fn m k))]
      (assoc-impl pm
                  #?@(:clj [:T_without_k T_internal-dissoc]
                      :cljs [:T_-dissoc!_k T_internal-dissoc])))
    ; default
    (assoc-impl pm behavior-key handler-fn)))

(defn assoc [coll & {:as e}]
  (reduce-kv (fn [pm k handler] (p-assoc pm k handler)) coll e))

(defn p-dissoc [pm behavior-key]
  (case behavior-key
    :get (dissoc-impl pm #?@(:clj [:valAt_k :valAt_k_nf]
                             :cljs [:-lookup_k :-lookup_k_nf]))
    :assoc (dissoc-impl pm #?(:clj :assoc_k_v :cljs :-assoc_k_v))
    :assoc! (dissoc-impl pm #?(:clj :T_assoc_k_v :cljs :T_-assoc_k_v))
    :dissoc (dissoc-impl pm #?(:clj :without_k :cljs :-dissoc_k))
    :dissoc! (dissoc-impl pm #?(:clj :T_without_k :cljs :T_-dissoc_k))
    :contains? (dissoc-impl pm #?(:clj :containsKey_k :cljs :-contains-key?_k))
    :invoke (dissoc-impl pm :invoke-variadic)
    (dissoc-impl pm behavior-key)))

(defn dissoc [coll & ks]
  (reduce (fn [pm k] (p-dissoc pm k)) coll ks))
