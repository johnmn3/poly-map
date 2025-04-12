(ns com.jolygon.poly-map.api-0
  (:refer-clojure :exclude [assoc dissoc])
  (:require
   [com.jolygon.poly-map.api-0.impl :as mi]
   [com.jolygon.poly-map.api-0.keys :as pm]))

(defn make-poly-map
  "Internal raw constructor for PolyMap. Creates a PolyMap instance
  directly from the underlying collection `m` and impls map
  `impls`. Does NOT perform the internal preparation step (like
  ensuring default-invoke). Prefer `com.jolygon.poly-map.api-0/poly-map`
  for general use."
  [m impls & [metadata]]
  (mi/poly-map* (or m {}) (or impls {}) metadata))

(defn make-transient-poly-map
  "Internal raw constructor for TransientPolyMap. Creates a TransientPolyMap
  instance directly from the underlying collection `m` and impls map
  `impls`. Does NOT perform the internal preparation step (like
  ensuring default-invoke). Prefer `com.jolygon.poly-map.api-0/poly-map`
  for general use."
  [edit m impls & [metadata]]
  (mi/transient-poly-map* edit (or m {}) (or impls {}) metadata))

(def empty-poly-map
  (mi/poly-map* {}))
#_empty-poly-map

(defn poly-map
  "keyval => key val
  Returns a new poly-map with supplied mappings.  If any keys are
  equal, they are handled as if by repeated uses of assoc."
  [& kvs]
  (->> kvs (partition 2) (mapv vec) (into empty-poly-map)))

(defn contains-impl?
  [coll k]
  (pm/-contains-impl? coll k))

(defn get-impl
  [coll k]
  (pm/-impl coll k))

(defn get-impls
  [coll]
  (pm/-get-impls coll))

(defn set-impls
  [coll new-impls]
  (pm/-set-impls coll new-impls))

(defn get-coll
  [coll]
  (pm/-get-coll coll))

(defn dissoc-impl
  [coll & ks]
  (->> ks
       (reduce (fn [c k]
                 (pm/-dissoc-impl c k))
               coll)))

(defn assoc-impl
  [coll & kvs]
  (->> kvs
       (partition 2)
       (reduce (fn [c [k v]]
                 (pm/-assoc-impl c k v))
               coll)))

;; High Level API

(defn p-assoc [poly-map behavior-key handler-fn]
  (case behavior-key
    :get
    (let [internal-get (fn [_this m _impls _metadata k] (handler-fn m k))
          internal-get-nf (fn [_this m _impls _metadata k nf] (handler-fn m k nf))]
      (assoc-impl poly-map
                  ::pm/invoke-variadic (fn invoke-variadic
                                         ([_this m _impls _metadata k]
                                          (handler-fn m k))
                                         ([_this m _impls _metadata k nf]
                                          (handler-fn m k nf)))
                  #?@(:clj [::pm/valAt_k internal-get
                            ::pm/valAt_k_nf internal-get-nf]
                      :cljs [::pm/-lookup_k internal-get
                             ::pm/-lookup_k_nf internal-get-nf])))
    :assoc
    (let [internal-assoc (fn [_this m impls metadata k v]
                           (let [new-m (handler-fn m k v)]
                             (make-poly-map new-m impls metadata)))]
      (assoc-impl poly-map
                  #?@(:clj [::pm/assoc_k_v internal-assoc]
                      :cljs [::pm/-assoc_k_v internal-assoc])))
    :dissoc
    (let [internal-dissoc (fn [_this m impls metadata k]
                            (let [new-m (handler-fn m k)]
                              (make-poly-map new-m impls metadata)))]
      (assoc-impl poly-map
                  #?@(:clj [::pm/without_k internal-dissoc]
                      :cljs [::pm/-without_k internal-dissoc])))
    :contains?
    (let [internal-contains (fn [_this m _impls _metadata k] (handler-fn m k))]
      (assoc-impl poly-map
                  #?@(:clj [::pm/containsKey_k internal-contains]
                      :cljs [::pm/-contains-key?_k internal-contains])))
    :invoke
    (let [internal-invoke (fn [_this m _impls _metadata & args] (apply handler-fn m args))]
      (assoc-impl poly-map ::pm/invoke-variadic internal-invoke))

    :print
    (let [internal-to-string (fn [_this m _impls _metadata]
                               (handler-fn m))
          internal-pr-writer (fn [_this m _impls _metadata writer & [_opts]]
                               (let [s ^String (handler-fn m)]
                                 #?(:clj  (.write ^java.io.Writer writer s)
                                    :cljs (cljs.core/-write writer s))))]
      (assoc-impl poly-map
                  ::pm/toString internal-to-string
                  #?@(:clj  [::pm/print-method_writer internal-pr-writer]
                      :cljs [::pm/-pr-writer_writer_opts internal-pr-writer])))
    ; default
    (let [internal-invoke (fn [_this m _impls _metadata & args] (apply handler-fn m args))]
      (assoc-impl poly-map behavior-key internal-invoke))))

(defn assoc [coll & {:as impls}]
  (reduce-kv (fn [pm k handler] (p-assoc pm k handler)) coll impls))

(defn p-dissoc [poly-map behavior-key]
  (case behavior-key
    :get (dissoc-impl poly-map ::pm/valAt_k ::pm/valAt_k_nf)
    :assoc (dissoc-impl poly-map ::pm/assoc_k_v)
    :dissoc (dissoc-impl poly-map ::pm/without_k)
    :contains? (dissoc-impl poly-map ::pm/containsKey_k)
    :invoke (dissoc-impl poly-map ::pm/invoke-variadic)
    (dissoc-impl poly-map behavior-key)))

(defn dissoc [coll & ks]
  (reduce (fn [pm k] (p-dissoc pm k)) coll ks))
