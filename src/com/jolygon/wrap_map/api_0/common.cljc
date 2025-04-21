(ns com.jolygon.wrap-map.api-0.common
  (:refer-clojure :exclude [count empty seq iterator get assoc dissoc meta reduce])
  (:require
    [clojure.core :as c]))

(defn ^:private default-map-invoke
  "Default IFn invoke behavior for WrapMap when no custom :invoke-variadic
  is provided. Mimics standard map lookup behavior: (map key) looks up key (arity 1),
  (map key nf) provides default (arity 2). Throws exceptions for all
  other arities (0, 3+)."
  ;; Arity 0: Invalid for map lookup
  ([_ _m]
   (throw (ex-info "Invalid arity: 0"
                   {:error :invalid-arity
                    :arity 0
                    :args []})))
  ;; Arity 1: Standard map lookup (key)
  ([_ m k]
   (c/get m k)) ;; Use aliased c/get
  ;; Arity 2: Standard map lookup (key, nf)
  ([_ m k nf]
   (c/get m k nf)) ;; Use aliased c/get
  ;; Arity 3: Invalid for map lookup
  ([_ _m a1 a2 a3]
   (throw (ex-info "Invalid arity: 3"
                   {:error :invalid-arity
                    :arity 3
                    :args [a1 a2 a3]})))
  ;; Variadic Arity (5+): Invalid for map lookup
  ([_ _m a1 a2 a3 a4 & rest-args]
   (let [;; Calculate the actual total arity
         arity (+ 4 (c/count rest-args))
         ;; Combine all arguments for the error map
         all-args (concat [a1 a2 a3 a4] rest-args)]
     (throw (ex-info (str "Invalid arity: " arity)
                     {:error :invalid-arity
                      :arity arity
                      :args all-args})))))

;; needs optimization
(defn handle-invoke
  "Core IFn invocation handler for WrapMap instances.
  Checks for :invoke-variadic in the environment map `e`
  and calls it if present with exact arity arguments
  (0-20). Otherwise delegates to default-map-invoke with
  exact arity arguments. Uses apply only for arity > 20."
  ([e m]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m)
     (default-map-invoke e m)))
  ([e m a1]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1)
     (default-map-invoke e m a1)))
  ([e m a1 a2]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2)
     (default-map-invoke e m a1 a2)))
  ([e m a1 a2 a3]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3)
     (default-map-invoke e m a1 a2 a3)))
  ([e m a1 a2 a3 a4]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4)
     (default-map-invoke e m a1 a2 a3 a4)))
  ([e m a1 a2 a3 a4 a5]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5)
     (default-map-invoke e m a1 a2 a3 a4 a5)))
  ([e m a1 a2 a3 a4 a5 a6]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6)))
  ([e m a1 a2 a3 a4 a5 a6 a7]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8 a9]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16)
     (default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16)))
  ([e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16 & rest-args]
   (if-let [wrap-invoke (c/get e :invoke-variadic)]
     (apply wrap-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16 rest-args)
     (apply default-map-invoke e m a1 a2 a3 a4 a5 a6 a7 a8 a9 a10 a11 a12 a13 a14 a15 a16 rest-args))))

;; --- Protocols ---

(defprotocol IWrapAssociative
  "Protocol for managing the environment map ('e') of a persistent wrap map."
  (-assoc-impl [coll k v] "Associates impl k with function v in the environment map. Returns new wrap map.")
  (-contains-impl? [coll k] "Returns true if impl k exists in the environment map.")
  (-impl [coll k] "Returns the function associated with impl k, or nil.")
  (-get-impls [coll] "Returns the full persistent environment map.")
  (-with-wrap [coll new-impls] "Replaces the entire invironment map. Returns new wrap map.")
  (-vary [coll afn args] "Like vary-meta but for the invironment map. Returns new wrap map.")
  (-unwrap [coll] "Returns the underlying persistent data collection ('m').")
  (-freeze [coll] "Returns a version where `e` cannot be changed.")
  (-dissoc-impl [coll k] "Removes impl k from the envionment map. Returns new wrap map."))
