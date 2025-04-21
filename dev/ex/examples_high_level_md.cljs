(ns ex.examples-high-level-md
  (:require
    [clojure.spec.alpha :as s]
    [com.jolygon.wrap-map :as w]))

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 1. Default Values for Missing Keys
;;
;;;;;;;;;;;;;;;;;;;;

(def default-value-map
  (-> {}
      (w/assoc :get (fn [m k & [nf]]
                      (get m k (or nf :not-available))))))

(def m1 (assoc default-value-map :a 1))

;; ### Example:

(get m1 :a) ;=> 1
(m1 :a) ;=> 1
(:a m1) ;=> 1

(get m1 :b) ;=> :not-available
(m1 :b) ;=> :not-available
(:b m1) ;=> :not-available

(get m1 :b :soon) ;=> :soon
(m1 :b :soon) ;=> :soon
(:b m1 :soon) ;=> :soon

;; ------------------------------------

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 2. Case-Insensitive String Keys
;;
;;;;;;;;;;;;;;;;;;;;

(defn- normalize-key [k]
  (if (string? k) (.toLowerCase ^String k) k))

(def case-insensitive-map
  (-> {}
      (w/assoc
        :assoc (fn [m k v] (assoc m (normalize-key k) v))
        :dissoc (fn [m k] (dissoc m (normalize-key k)))
        :contains? (fn [m k] (contains? m (normalize-key k)))
        :get (fn [m k & [nf]] (get m (normalize-key k) nf)))))

(def headers
  (-> case-insensitive-map
      (assoc "Content-Type" "application/json")
      (assoc :other-header 123)))

;; ### Example:

(get headers "content-type") ;=> "application/json"
(get headers "CONTENT-TYPE") ;=> "application/json"
(contains? headers "Content-type") ;=> true

(get headers :other-header) ;=> 123 (Non-string keys unaffected)

(dissoc headers "CONTENT-TYPE") ;=> {:other-header 123}

;; ------------------------------------

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 3. Schema Validation on Assoc
;;
;;;;;;;;;;;;;;;;;;;;

(require '[clojure.spec.alpha :as s])

(s/def ::name string?)
(s/def ::age pos-int?)
#_(s/def ::user (s/keys :req-un [::name ::age]))

(def schema-map
  (-> {}
      (w/assoc
        :assoc (fn [m k v]
                 (let [expected-type (case k :name ::name :age ::age :any)]
                   (if (or (= expected-type :any) (s/valid? expected-type v))
                     (assoc m k v)
                     (throw (ex-info "Schema validation failed"
                                     {:key k :value v :expected (s/describe expected-type)}))))))))

;; ### Example:

(def user (-> schema-map (assoc :name "Alice") (assoc :age 30)))
;=> {:name "Alice", :age 30}

(try
  (assoc user :age -5)
  (catch :default e (ex-data e)))
;=> {:key :age, :value -5, :expected pos-int?}

(try
  (assoc user :name 123)
  (catch :default e (ex-data e)))
;=> {:key :name, :value 123, :expected string?}

;; ------------------------------------

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 4. Logging Accesses (Read Logging)
;;
;;;;;;;;;;;;;;;;;;;;

(def access-log (atom []))

(def logging-read-map
  (-> {}
      (w/assoc :get (fn [m k & [nf]]
                      (swap! access-log conj (if nf [:get k nf] [:get k]))
                      (get m k nf)))))

(def mlog (assoc logging-read-map :a 1))

;; ### Example:

(reset! access-log [])
(get mlog :a) ;=> 1
(get mlog :b) ;=> nil (Logged as [:get :b])
(get mlog :c 404) ;=> 404
@access-log
;=> [[:get :a] [:get :b] [:get :c 404]]

;; ------------------------------------

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 5. Side Effects on Update
;;
;;;;;;;;;;;;;;;;;;;;

(defn notify-change [change-type k value]
  (println "[Notification] Type:" change-type ", Key:" k ", Value:" value))

(def notifying-map
  (-> {}
      (w/assoc
        :assoc (fn [m k v]
                 (notify-change :assoc k v)
                 (assoc m k v))
        :dissoc (fn [m k]
                  (notify-change :dissoc k nil)
                  (dissoc m k)))))

;; ### Example:

(def nmap1 (assoc notifying-map :user "admin"))
; [Notification] Type: :assoc , Key: :user , Value: admin
(def nmap2 (dissoc nmap1 :user))
; [Notification] Type: :dissoc , Key: :user , Value: nil
(def nmap3 (assoc nmap2 :user2 "user"))
; [Notification] Type: :dissoc , Key: :user2 , Value: nil

;; ------------------------------------

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 6. Computed / Virtual Properties
;;
;;;;;;;;;;;;;;;;;;;;

(def computed-prop-map
  (-> {:first-name "Jane" :last-name "Doe"}
      (w/assoc :get (fn [m k & [nf]]
                      (if (= k :full-name)
                         ;; Compute value for :full-name
                        (str (:first-name m) " " (:last-name m))
                         ;; Otherwise, standard lookup
                        (get m k nf))))))

;; ### Example:

(get computed-prop-map :first-name) ;=> "Jane"
(get computed-prop-map :full-name) ;=> "Jane Doe"
(get computed-prop-map :age :unknown) ;=> :unknown
(get computed-prop-map :full-name :unknown) ;=> "Jane Doe"

;; ------------------------------------

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 7. Lazy Loading from External Source
;;
;;;;;;;;;;;;;;;;;;;;

(defn simulate-db-fetch [k]
  (println "[DB] Fetching data for key:" k)
  ;; (Thread/sleep 50) ; Simulate delay ;; not in CLJS
  (if (= k :user-prefs) {:theme "dark" :lang "en"} nil))

(def lazy-loading-map
  (-> {}
      (w/assoc :get (fn [m k & [nf]]
                      (let [v (get m k ::nf)]
                        (if (= v ::nf)
                          ;; Not found locally, try loading
                          (if-let [loaded-val (simulate-db-fetch k)]
                            ;; Found externally: assoc into a new map and return the value
                            ;; This effectively caches the result.
                            (do
                              (println "[Cache] Storing loaded value for key:" k)
                              loaded-val) ;; Simple version: just return loaded, no cache update
                            ;; Not found externally either
                            (or nf ::nf))
                          ;; Found locally
                          v))))))

;; ### Example:

(def lazy-map (assoc lazy-loading-map :config {:port 80}))

(get lazy-map :config) ;=> {:port 80} (No fetch)

(get lazy-map :user-prefs)
; [DB] Fetching data for key: :user-prefs
; [Cache] Storing loaded value for key: :user-prefs
;=> {:theme "dark", :lang "en"}

(get lazy-map :user-prefs) ; Access again
; [DB] Fetching data for key: :user-prefs (Fetched again as simple version doesn't cache)
; [Cache] Storing loaded value for key: :user-prefs
;=> {:theme "dark", :lang "en"}

(get lazy-map :other-key :default)
; [DB] Fetching data for key: :other-key
;=> :default

;; ------------------------------------

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 9. Function Call Dispatch
;;
;;;;;;;;;;;;;;;;;;;;

(defn handle-add [x y] (+ x y))
(defn handle-multiply [x y] (* x y))

(def dispatching-map
  (-> {:add-fn handle-add :mul-fn handle-multiply}
      (w/assoc :invoke (fn [m operation & args]
                         (case operation
                           :add (apply (:add-fn m) args)
                           :multiply (apply (:mul-fn m) args)
                           (throw (ex-info "Unknown operation" {:operation operation})))))))

;; ### Example:

(dispatching-map :add 10 5) ;=> 15
(dispatching-map :multiply 10 5) ;=> 50

(try (dispatching-map :subtract 10 5) (catch :default e (ex-data e)))
;=> {:operation :subtract}

;; ------------------------------------

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 10. Access Counting
;;
;;;;;;;;;;;;;;;;;;;;

(def access-counts (atom {}))

(def counting-map
  (-> {:a 1 :b 2}
      (w/assoc :get (fn [m k & [nf]]
                      (swap! access-counts update k (fnil inc 0))
                      (get m k nf)))))

;; ### Example:

(reset! access-counts {})
(get counting-map :a) ;=> 1
(get counting-map :b) ;=> 2
(get counting-map :a) ;=> 1
(get counting-map :c) ;=> nil
@access-counts
;=> {:a 2, :b 1, :c 1}

;; ------------------------------------

;;;;;;;;;;;;;;;;;;;;
;;
;; ## 12. Custom String Representation
;;
;;;;;;;;;;;;;;;;;;;;

(def sanitizing-string-map
  (-> {:user "secret-user" :id 123 :data [1 2 3]}
      (w/assoc :print #(str "<SecureMapData id=" (:id %) ">"))))

;; ### Example:

(str sanitizing-string-map)
;=> "<SecureMapData id=123>"

(println sanitizing-string-map)
; <SecureMapData id=123>

;; ------------------------------------
