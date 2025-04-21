(ns com.jolygon.wrap-map.api-0-test
  (:require
    [clojure.test :refer [deftest is]]
    [com.jolygon.wrap-map.api-0 :as w :refer [empty-wrap wrap]]))

(deftest wrap-map-build-test
  (is (= (type empty-wrap) (type (wrap))))
  (is (= {:a 1, :b 2} (wrap :a 1, :b 2)))
  (is (= {:a 1, :b 2} (wrap :b 2, :a 1)))
  (is (= {:a 1, :b 2, :c 3} (wrap :a 1, :b 2, :c 3)))
  (is (= {:a 1, :b 2, :c 3} (wrap :c 3, :a 1, :b 2)))
  (is (= {:a 1, :b 2, :c 3} (wrap :c 3, :b 2, :a 1)))
  (is (= {:a 1, :b 2, :c 3} (wrap :b 2, :c 3, :a 1))))

(deftest wrap-map-arity-test
  (is (= "clojure.lang.ExceptionInfo: Invalid arity: 0 {:error :invalid-arity, :arity 0, :args []}"
         (try ((wrap)) (catch Exception e (str e)))))
  (is (= 1 ((wrap :a 1) :a)))
  (is (= nil ((wrap :a 1) :b)))
  (is (= "clojure.lang.ExceptionInfo: Invalid arity: 3 {:error :invalid-arity, :arity 3, :args [1 2 3]}"
         (try ((wrap) 1 2 3) (catch Exception e (str e)))))
  (is (= "clojure.lang.ExceptionInfo: Invalid arity: 4 {:error :invalid-arity, :arity 4, :args (1 2 3 4)}"
         (try ((wrap) 1 2 3 4) (catch Exception e (str e))))))

(deftest wrap-map-assoc-dissoc-test
  (is (= {:a 1, :b 2} (assoc (wrap :a 1) :b 2)))
  (is (= (type empty-wrap)
         (type (assoc (wrap :a 1) :b 2))))

  (is (= {:a 1} (dissoc (wrap :a 1 :b 2) :b)))
  (is (= (type empty-wrap)
         (type (dissoc (wrap :a 1 :b 2) :b))))

  (is (= {:a 1, :b 2} (merge (wrap :a 1) {:b 2})))
  (is (= (type empty-wrap)
         (type (merge (wrap :a 1) {:b 2})))))

(deftest wrap-map-conj-test
  (is (= (conj (wrap) {}) (wrap)))
  (is (= (conj (wrap) {:a 1}) (wrap :a 1)))
  (is (= (conj (wrap) {:a 1} {:b 2}) (wrap :a 1 :b 2)))
  (is (= (conj (wrap) {:a 1} {:b 2 :c 3}) (wrap :a 1 :b 2 :c 3)))

  (is (= (conj (wrap :a 1) {}) (wrap :a 1)))
  (is (= (conj (wrap :a 1) {:b 2}) (wrap :a 1 :b 2)))
  (is (= (conj (wrap :a 1) {:b 2} {:c 3}) (wrap :a 1 :b 2 :c 3)))

  (is (= (conj (wrap) (first (wrap :a 1)))
         (wrap :a 1)))
  (is (= (conj (wrap :b 2) (first (wrap :a 1)))
         (wrap :a 1 :b 2)))
  (is (= (conj (wrap :b 2) (first (wrap :a 1)) (first (wrap :c 3)))
         (wrap :a 1 :b 2 :c 3)))

  (is (= (conj (wrap) [:a 1])
         (wrap :a 1)))
  (is (= (conj (wrap :b 2) [:a 1])
         (wrap :a 1 :b 2)))
  (is (= (conj (wrap :b 2) [:a 1] [:c 3])
         (wrap :a 1 :b 2 :c 3)))

  (is (= (conj (wrap) (wrap nil (wrap)))
         (wrap nil (wrap))))
  (is (= (conj (wrap) (wrap (wrap) nil))
         (wrap (wrap) nil)))
  (is (= (conj (wrap) (wrap (wrap) (wrap)))
         (wrap (wrap) (wrap)))))

(deftest wrap-map-find-test
  (is (= (conj (wrap) {}) (wrap)))
  (is (= (find (wrap) :a) nil))
  (is (= (find (wrap :a 1) :a) [:a 1]))
  (is (= (find (wrap :a 1) :b) nil))
  (is (= (find (wrap nil 1) nil) [nil 1]))
  (is (= (find (wrap :a 1 :b 2) :a) [:a 1]))
  (is (= (find (wrap :a 1 :b 2) :b) [:b 2]))
  (is (= (find (wrap :a 1 :b 2) :c) nil))
  (is (= (find (wrap) nil) nil))
  (is (= (find (wrap :a 1) nil) nil))
  (is (= (find (wrap :a 1 :b 2) nil) nil)))

(deftest wrap-map-contains-test
  (is (= (contains? (wrap) :a) false))
  (is (= (contains? (wrap) nil) false))
  (is (= (contains? (wrap :a 1) :a) true))
  (is (= (contains? (wrap :a 1) :b) false))
  (is (= (contains? (wrap :a 1) nil) false))
  (is (= (contains? (wrap nil 1) nil) true))
  (is (= (contains? (wrap :a 1 :b 2) :a) true))
  (is (= (contains? (wrap :a 1 :b 2) :b) true))
  (is (= (contains? (wrap :a 1 :b 2) :c) false))
  (is (= (contains? (wrap :a 1 :b 2) nil) false)))

(deftest wrap-map-keys-vals-test
  (is (= (keys (wrap)) nil))
  (is (= (keys (wrap :a 1)) '(:a)))
  (is (= (keys (wrap nil 1)) '(nil)))
  (is (= (vals (wrap)) nil))
  (is (= (vals (wrap :a 1)) '(1)))
  (is (= (vals (wrap nil 1)) '(1))))
#_{:clj-kondo/ignore [:single-key-in]}
(deftest wrap-map-get-test
  (let [m (wrap :a 1, :b 2, :c {:d 3, :e 4}, :f nil, :g false, nil {:h 5})]
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

(deftest wrap-map-destructure-test
  (let [sample-map (wrap :a 1 :b {:a 2})
        {ao1 :a {ai1 :a} :b} sample-map
        {ao2 :a {ai2 :a :as _m1} :b :as _m2} sample-map
        {ao3 :a {ai3 :a :as _m} :b :as _m} sample-map
        {{ai4 :a :as _m} :b ao4 :a :as _m} sample-map]
    (is (and (= 2 ai1) (= 1 ao1)))
    (is (and (= 2 ai2) (= 1 ao2)))
    (is (and (= 2 ai3) (= 1 ao3)))
    (is (and (= 2 ai4) (= 1 ao4)))))

(deftest test-wrap-map-impls
  (let [dm (w/with-wrap (wrap :a 1 :b 2)
             {:invoke (fn [_env & args] (apply + args))})]

    (is (w/contains-impl? dm :invoke) "Should contain :invoke impl")
    (is (not (w/contains-impl? dm :non-existent)) "Should not contain :non-existent impl")

    (is (fn? (w/get-impl dm :invoke)) ":invoke impl should be a function")
    (is (nil? (w/get-impl dm :non-existent)) "Non-existent impl should return nil")

    (let [e (w/get-impls dm)]
      (is (map? e) "get-impls should return a map")
      (is (contains? e :invoke) "Implementations should contain :invoke"))

    (is (= {:a 1 :b 2} (w/unwrap dm)) "unwrap should return the underlying collection")

    (let [updated-dm (w/dissoc-impl dm :invoke)]
      (is (not (w/contains-impl? updated-dm :invoke)) "Implementations should be removed after dissoc-impl"))))

;; --- Tests for Implementation Manipulation API ---

(deftest test-impl-api-persistent
  (let [m0 w/empty-wrap
        f1 (fn [_ _] "f1")
        f2 (fn [_ _ k] (str "f2-" k))]
    (is (not (w/contains-impl? m0 :valAt_k)) "Empty map shouldn't contain impl")
    (is (nil? (w/get-impl m0 :valAt_k)) "Getting non-existent impl returns nil")

    (let [m1 (w/assoc-impl m0 :valAt_k f1)]
      (is (w/contains-impl? m1 :valAt_k) "Should contain impl after assoc-impl")
      (is (= f1 (w/get-impl m1 :valAt_k)) "Should retrieve correct impl")
      (is (map? (w/get-impls m1)) "get-impls returns a map")
      (is (= f1 (get (w/get-impls m1) :valAt_k)) "get-impls includes added impl"))

    (let [m2 (w/assoc-impl m0 :valAt_k f1 :valAt_k_nf f2)]
      (is (w/contains-impl? m2 :valAt_k) "Contains first impl")
      (is (w/contains-impl? m2 :valAt_k_nf) "Contains second impl")
      (is (= f1 (w/get-impl m2 :valAt_k)))
      (is (= f2 (w/get-impl m2 :valAt_k_nf))))

    (let [m1 (w/assoc-impl m0 :valAt_k f1)
          m3 (w/dissoc-impl m1 :valAt_k)]
      (is (not (w/contains-impl? m3 :valAt_k)) "Should not contain impl after dissoc-impl"))

    (let [m1 (w/assoc m0 :valAt_k f1)
          new-impls {:assoc_k_v f2}
          m4 (w/with-wrap m1 new-impls)]
      (is (not (w/contains-impl? m4 :valAt_k)) "Old impl gone after with-wrap")
      (is (w/contains-impl? m4 :assoc_k_v) "New impl present after with-wrap"))))

;; --- Tests for Common Override Scenarios ---

(deftest test-override-get-default-value
  (let [default-val :i-am-default
        m (w/assoc
            (wrap :a 1)
            :valAt_k_nf
            (fn [_ m k _nf]
              (let [v (get m k ::nf)]
                (if (= v ::nf)
                  default-val ;; Return custom default
                  v)))
            :valAt_k
            (fn [_ m k]
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
        (w/assoc-impl
          w/empty-wrap
          :assoc_k_v
          (fn [{:as e :keys [<-]} m k v]
            (if (string? v)
             ;; Construct new map instance - using wrap constructor for now
              (<- e (assoc m k (str "Validated: " v)))
              (throw (ex-info "Validation failed: Value must be string" {:key k :value v})))))]
    (let [m1 (assoc validated-map :a "hello")]
      (is (= {:a "Validated: hello"} m1))
      (is (instance? com.jolygon.wrap_map.api_0.impl.WrapMap+assoc_k_v m1)))
    (is (thrown? clojure.lang.ExceptionInfo (assoc validated-map :b 123)))))

(deftest test-override-invoke-variadic
  (let [callable-map
        (w/assoc
          (wrap :base 10)
          :invoke-variadic
          (fn [_ m & args]
            (+ (:base m) (apply + args))))]
    (is (= 10 (callable-map)) "Invoke with 0 args")
    (is (= 15 (callable-map 5)) "Invoke with 1 arg")
    (is (= 16 (callable-map 1 2 3)) "Invoke with multiple args")
    (is (= 10 (get callable-map :base)) "Lookup still works (invoke not called for arity 1/2 by default)")
    (is (= :nf (get callable-map :missing :nf)) "Lookup still works")))

(deftest test-override-transient-logging
  (let [log (atom [])
        logging-map
        (w/assoc
          (wrap)
          :T_assoc_k_v
          (fn [_ t-m k v]
            (swap! log conj [:assoc! k v])
            (assoc! t-m k v))
          :T_without_k
          (fn [_ t-m k]
            (swap! log conj [:without! k])
            (dissoc! t-m k)))
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
  (let [m (w/assoc
            (wrap :a 1 :b 2)
            :toString
            (fn [_ m]
              (str "<WrapMap:" (count m) " entries>")))]
    (is (= "<WrapMap:2 entries>" (str m)))))

(comment
  ;;; runnning tests
  (do
    (wrap-map-build-test)
    (wrap-map-arity-test)
    (wrap-map-assoc-dissoc-test)
    (wrap-map-conj-test)
    (wrap-map-find-test)
    (wrap-map-contains-test)
    (wrap-map-keys-vals-test)
    (wrap-map-get-test)
    (wrap-map-destructure-test)
    (test-wrap-map-impls)
    (test-impl-api-persistent)
    (test-override-get-default-value)
    (test-override-assoc-validation)
    (test-override-invoke-variadic)
    (test-override-transient-logging)
    (test-override-toString))

  :end)
