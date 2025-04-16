(ns com.jolygon.poly-map.api-0-test
  (:require
    [clojure.test :refer [deftest is]]
    [com.jolygon.poly-map.api-0 :as poly :refer [empty-poly-map poly-map]]
    [com.jolygon.poly-map.api-0.impl :as mi]
    [com.jolygon.poly-map.api-0.keys :as pm]
    [com.jolygon.poly-map.api-0.trans.keys :as tpm]))

(deftest poly-map-build-test
  (is (= (type empty-poly-map) (type (poly-map))))
  (is (= {:a 1, :b 2} (poly-map :a 1, :b 2)))
  (is (= {:a 1, :b 2} (poly-map :b 2, :a 1)))
  (is (= {:a 1, :b 2, :c 3} (poly-map :a 1, :b 2, :c 3)))
  (is (= {:a 1, :b 2, :c 3} (poly-map :c 3, :a 1, :b 2)))
  (is (= {:a 1, :b 2, :c 3} (poly-map :c 3, :b 2, :a 1)))
  (is (= {:a 1, :b 2, :c 3} (poly-map :b 2, :c 3, :a 1))))

(deftest poly-map-arity-test
  (is (= "invalid-arity 0"
         (try ((poly-map)) (catch :default e
                             (str (name (:error (ex-data e)))
                                  " "
                                  (:arity (ex-data e)))))))
  (is (= 1 ((poly-map :a 1) :a)))
  (is (= nil ((poly-map :a 1) :b)))
  (is (= "invalid-arity 3"
         (try ((poly-map) 1 2 3) (catch :default e
                                   (str (name (:error (ex-data e)))
                                        " "
                                        (:arity (ex-data e)))))))
  (is (= "invalid-arity 4"
         (try ((poly-map) 1 2 3 4) (catch :default e
                                     (str (name (:error (ex-data e)))
                                          " "
                                          (:arity (ex-data e))))))))

(deftest poly-map-assoc-dissoc-test
  (is (= {:a 1, :b 2} (assoc (poly-map :a 1) :b 2)))
  (is (= (type empty-poly-map)
         (type (assoc (poly-map :a 1) :b 2))))

  (is (= {:a 1} (dissoc (poly-map :a 1 :b 2) :b)))
  (is (= (type empty-poly-map)
         (type (dissoc (poly-map :a 1 :b 2) :b))))

  (is (= {:a 1, :b 2} (merge (poly-map :a 1) {:b 2})))
  (is (= (type empty-poly-map)
         (type (merge (poly-map :a 1) {:b 2})))))

(deftest poly-map-conj-test
  (is (= (conj (poly-map) {}) (poly-map)))
  (is (= (conj (poly-map) {:a 1}) (poly-map :a 1)))
  (is (= (conj (poly-map) {:a 1} {:b 2}) (poly-map :a 1 :b 2)))
  (is (= (conj (poly-map) {:a 1} {:b 2 :c 3}) (poly-map :a 1 :b 2 :c 3)))

  (is (= (conj (poly-map :a 1) {}) (poly-map :a 1)))
  (is (= (conj (poly-map :a 1) {:b 2}) (poly-map :a 1 :b 2)))
  (is (= (conj (poly-map :a 1) {:b 2} {:c 3}) (poly-map :a 1 :b 2 :c 3)))

  (is (= (conj (poly-map) (first (poly-map :a 1)))
         (poly-map :a 1)))
  (is (= (conj (poly-map :b 2) (first (poly-map :a 1)))
         (poly-map :a 1 :b 2)))
  (is (= (conj (poly-map :b 2) (first (poly-map :a 1)) (first (poly-map :c 3)))
         (poly-map :a 1 :b 2 :c 3)))

  (is (= (conj (poly-map) [:a 1])
         (poly-map :a 1)))
  (is (= (conj (poly-map :b 2) [:a 1])
         (poly-map :a 1 :b 2)))
  (is (= (conj (poly-map :b 2) [:a 1] [:c 3])
         (poly-map :a 1 :b 2 :c 3)))

  (is (= (conj (poly-map) (poly-map nil (poly-map)))
         (poly-map nil (poly-map))))
  (is (= (conj (poly-map) (poly-map (poly-map) nil))
         (poly-map (poly-map) nil)))
  (is (= (conj (poly-map) (poly-map (poly-map) (poly-map)))
         (poly-map (poly-map) (poly-map)))))

(deftest poly-map-find-test
  (is (= (conj (poly-map) {}) (poly-map)))
  (is (= (find (poly-map) :a) nil))
  (is (= (find (poly-map :a 1) :a) [:a 1]))
  (is (= (find (poly-map :a 1) :b) nil))
  (is (= (find (poly-map nil 1) nil) [nil 1]))
  (is (= (find (poly-map :a 1 :b 2) :a) [:a 1]))
  (is (= (find (poly-map :a 1 :b 2) :b) [:b 2]))
  (is (= (find (poly-map :a 1 :b 2) :c) nil))
  (is (= (find (poly-map) nil) nil))
  (is (= (find (poly-map :a 1) nil) nil))
  (is (= (find (poly-map :a 1 :b 2) nil) nil)))

(deftest poly-map-contains-test
  (is (= (contains? (poly-map) :a) false))
  (is (= (contains? (poly-map) nil) false))
  (is (= (contains? (poly-map :a 1) :a) true))
  (is (= (contains? (poly-map :a 1) :b) false))
  (is (= (contains? (poly-map :a 1) nil) false))
  (is (= (contains? (poly-map nil 1) nil) true))
  (is (= (contains? (poly-map :a 1 :b 2) :a) true))
  (is (= (contains? (poly-map :a 1 :b 2) :b) true))
  (is (= (contains? (poly-map :a 1 :b 2) :c) false))
  (is (= (contains? (poly-map :a 1 :b 2) nil) false)))

(deftest poly-map-keys-vals-test
  (is (= (keys (poly-map)) nil))
  (is (= (keys (poly-map :a 1)) '(:a)))
  (is (= (keys (poly-map nil 1)) '(nil)))
  (is (= (vals (poly-map)) nil))
  (is (= (vals (poly-map :a 1)) '(1)))
  (is (= (vals (poly-map nil 1)) '(1))))
#_{:clj-kondo/ignore [:single-key-in]}
(deftest poly-map-get-test
  (let [m (poly-map :a 1, :b 2, :c {:d 3, :e 4}, :f nil, :g false, nil {:h 5})]
    (is (= (get m :a) 1))
    (is (= (get m :e) nil))
    (is (= (get m :e 0) 0))
    (is (= (get m nil) {:h 5}))
    (is (= (get m :b 0) 2))
    (is (= (get m :f 0) nil))
    (is (= (get-in m [:c :e]) 4))
    (is (= (get-in m '(:c :e)) 4))
    (is (= (get-in m [:c :x]) nil))
    (is (= (get-in m [:f]) nil))
    (is (= (get-in m [:g]) false))
    (is (= (get-in m [:h]) nil))
    (is (= (get-in m []) m))
    (is (= (get-in m nil) m))
    (is (= (get-in m [:c :e] 0) 4))
    (is (= (get-in m '(:c :e) 0) 4))
    (is (= (get-in m [:c :x] 0) 0))
    (is (= (get-in m [:b] 0) 2))
    (is (= (get-in m [:f] 0) nil))
    (is (= (get-in m [:g] 0) false))
    (is (= (get-in m [:h] 0) 0))
    (is (= (get-in m [:x :y] {:y 1}) {:y 1}))
    (is (= (get-in m [] 0) m))
    (is (= (get-in m nil 0) m))))

(deftest poly-map-destructure-test
  (let [sample-map (poly-map :a 1 :b {:a 2})
        {ao1 :a {ai1 :a} :b} sample-map
        {ao2 :a {ai2 :a :as _m1} :b :as _m2} sample-map
        {ao3 :a {ai3 :a :as _m} :b :as _m} sample-map
        {{ai4 :a :as _m} :b ao4 :a :as _m} sample-map]
    (is (and (= 2 ai1) (= 1 ao1)))
    (is (and (= 2 ai2) (= 1 ao2)))
    (is (and (= 2 ai3) (= 1 ao3)))
    (is (and (= 2 ai4) (= 1 ao4)))))

(deftest test-poly-map-impls
  (let [dm (poly/set-impls (poly-map :a 1 :b 2)
                           {::poly/invoke (fn [_env & args] (apply + args))})]

    (is (poly/contains-impl? dm ::poly/invoke) "Should contain ::poly/invoke impl")
    (is (not (poly/contains-impl? dm :non-existent)) "Should not contain :non-existent impl")

    (is (fn? (poly/get-impl dm ::poly/invoke)) "::poly/invoke impl should be a function")
    (is (nil? (poly/get-impl dm :non-existent)) "Non-existent impl should return nil")

    (let [impls (poly/get-impls dm)]
      (is (map? impls) "get-impls should return a map")
      (is (contains? impls ::poly/invoke) "Implementations should contain ::poly/invoke"))

    (let [new-impls {::new-impl (fn [] "new")}
          updated-dm (poly/set-impls dm new-impls)]
      (is (poly/contains-impl? updated-dm ::new-impl) "Should contain new impl after set-impls")
      (is (not (poly/contains-impl? updated-dm ::poly/invoke)) "Should not contain old impl after set-impls"))

    (is (= {:a 1 :b 2} (poly/get-coll dm)) "get-coll should return the underlying collection")

    (let [updated-dm (poly/dissoc-impl dm ::poly/invoke)]
      (is (not (poly/contains-impl? updated-dm ::poly/invoke)) "Implementation should be removed after dissoc-impl"))

    (let [updated-dm (poly/assoc-impl dm ::new-impl (fn [] "new"))]
      (is (poly/contains-impl? updated-dm ::new-impl) "New impl should be added after assoc-impl")
      (is (= "new" ((poly/get-impl updated-dm ::new-impl))) "New impl should be callable"))))

;; --- Tests for Implementation Manipulation API ---

(deftest test-impl-api-persistent
  (let [m0 poly/empty-poly-map
        f1 (fn [_ _] "f1")
        f2 (fn [_ _ k] (str "f2-" k))]
    (is (not (poly/contains-impl? m0 ::pm/get_k)) "Empty map shouldn't contain impl")
    (is (nil? (poly/get-impl m0 ::pm/get_k)) "Getting non-existent impl returns nil")

    (let [m1 (poly/assoc-impl m0 ::pm/get_k f1)]
      (is (poly/contains-impl? m1 ::pm/get_k) "Should contain impl after assoc-impl")
      (is (= f1 (poly/get-impl m1 ::pm/get_k)) "Should retrieve correct impl")
      (is (map? (poly/get-impls m1)) "get-impls returns a map")
      (is (= f1 (get (poly/get-impls m1) ::pm/get_k)) "get-impls includes added impl"))

    (let [m2 (poly/assoc-impl m0 ::pm/get_k f1 ::pm/get_k_nf f2)]
      (is (poly/contains-impl? m2 ::pm/get_k) "Contains first impl")
      (is (poly/contains-impl? m2 ::pm/get_k_nf) "Contains second impl")
      (is (= f1 (poly/get-impl m2 ::pm/get_k)))
      (is (= f2 (poly/get-impl m2 ::pm/get_k_nf))))

    (let [m1 (poly/assoc-impl m0 ::pm/get_k f1)
          m3 (poly/dissoc-impl m1 ::pm/get_k)]
      (is (not (poly/contains-impl? m3 ::pm/get_k)) "Should not contain impl after dissoc-impl")
      (is (empty? (poly/get-impls m3)) "Implementations map should be empty after dissoc"))

    (let [m1 (poly/assoc-impl m0 ::pm/get_k f1)
          new-impls {::pm/assoc_k_v f2}
          m4 (poly/set-impls m1 new-impls)]
      (is (not (poly/contains-impl? m4 ::pm/get_k)) "Old impl gone after set-impls")
      (is (poly/contains-impl? m4 ::pm/assoc_k_v) "New impl present after set-impls")
      (is (= new-impls (dissoc (poly/get-impls m4) ::pm/default-invoke-variadic))
          "get-impls reflects new impls (excluding default invoke)"))))

;; --- Tests for Common Override Scenarios ---
;; (def default-val :i-am-default)
(deftest test-override-get-default-value
  (let [default-val :i-am-default
        m (poly/assoc-impl
            (poly/poly-map :a 1)
            ::pm/-lookup_k_nf
            (fn [_this m _impls _metadata k _nf]
              (let [v (get m k ::nf)]
                (if (= v ::nf)
                  default-val ;; Return custom default
                  v)))
            ::pm/-lookup_k
            (fn [_this m _impls _metadata k]
              (let [v (get m k ::nf)] ;;<- same as above
                (if (= v ::nf)
                  default-val
                  v))))]
    (is (= 1 (get m :a)) "Getting existing key works normally")
    (is (= 1 (get m :a :wrong-default)) "Getting existing key ignores nf")
    (is (= default-val (get m :b)) "Getting missing key returns custom default")
    (is (= default-val (get m :b :wrong-default)) "Getting missing key returns custom default even if nf supplied")))

(deftest test-override-assoc-validation
  (let [validated-map
        (poly/assoc-impl
          poly/empty-poly-map
          ::pm/-assoc_k_v
          (fn [_this m impls _metadata k v]
            (if (string? v)
             ;; Construct new map instance - using impl constructor for now
              (poly/make-poly-map (assoc m k (str "Validated: " v)) impls)
              (throw (ex-info "Validation failed: Value must be string" {:key k :value v})))))]
    (let [m1 (assoc validated-map :a "hello")]
      (is (= {:a "Validated: hello"} m1))
      (is (instance? mi/PolyMap m1)))
    (is (thrown? :default (assoc validated-map :b 123)))))

(deftest test-override-invoke-variadic
  (let [callable-map
        (poly/assoc-impl
          (poly/poly-map :base 10)
          ::pm/invoke-variadic
          (fn [_this m _impls _metadata & args]
            (+ (:base m) (apply + args))))]
    (is (= 10 (callable-map)) "Invoke with 0 args")
    (is (= 15 (callable-map 5)) "Invoke with 1 arg")
    (is (= 16 (callable-map 1 2 3)) "Invoke with multiple args")
    (is (= 10 (get callable-map :base)) "Lookup still works (invoke not called for arity 1/2 by default)")
    (is (= :nf (get callable-map :missing :nf)) "Lookup still works")))

(deftest test-override-transient-logging
  (let [log (atom [])
        logging-map
        (poly/assoc-impl
          (poly/poly-map)
          ::tpm/-assoc!_k_v
          (fn [this t-m _t-impls _metadata k v]
            (swap! log conj [:assoc! k v])
            #_{:clj-kondo/ignore [:unused-value]}
            (assoc! t-m k v)
            this)
          ::tpm/-dissoc!_k
          (fn [this t-m _t-impls _metadata k]
            (swap! log conj [:without! k])
            #_{:clj-kondo/ignore [:unused-value]}
            (dissoc! t-m k)
            this))
        final-map (persistent!
                    (-> (transient logging-map)
                        (assoc! :a 1)
                        (assoc! :b 2)
                        (dissoc! :a)
                        (assoc! :c 3)))]
    (is (= {:b 2 :c 3} final-map) "Final map state is correct")
    (is (= [[:assoc! :a 1]
            [:assoc! :b 2]
            [:without! :a]
            [:assoc! :c 3]] @log) "Log contains correct operations")))

(deftest test-override-toString
  (let [m (poly/assoc-impl
            (poly/poly-map :a 1 :b 2)
            ::pm/toString
            (fn [_this m _impls _metadata]
              (str "<PolyMap:" (count m) " entries>")))]
    (is (= "<PolyMap:2 entries>" (str m)))))

(comment
  ;;; runnning tests
  (println :hi)
  (do
    (poly-map-build-test)
    (poly-map-arity-test)
    (poly-map-assoc-dissoc-test)
    (poly-map-conj-test)
    (poly-map-find-test)
    (poly-map-contains-test)
    (poly-map-keys-vals-test)
    (poly-map-get-test)
    (poly-map-destructure-test)
    (test-poly-map-impls)
    (test-impl-api-persistent)
    (test-override-get-default-value)
    (test-override-assoc-validation)
    (test-override-invoke-variadic)
    (test-override-transient-logging)
    (test-override-toString)))

(defn -main []
  (println :starting :test)
  (poly-map-build-test)
  (poly-map-arity-test)
  (poly-map-assoc-dissoc-test)
  (poly-map-conj-test)
  (poly-map-find-test)
  (poly-map-contains-test)
  (poly-map-keys-vals-test)
  (poly-map-get-test)
  (poly-map-destructure-test)
  (test-poly-map-impls)
  (test-impl-api-persistent)
  (test-override-get-default-value)
  (test-override-assoc-validation)
  (test-override-invoke-variadic)
  (test-override-transient-logging)
  (test-override-toString)
  (println :test :complete))
