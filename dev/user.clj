(ns user
  (:require
    [clojure.tools.namespace.repl :as repl]
    [clojure.test :as test]))

(repl/set-refresh-dirs "dev" "src" "test")

(defn reset
  "Reload changed namespaces."
  []
  (repl/refresh))

(defn run-all-tests
  "Run all tests."
  []
  (reset)
  (test/run-all-tests #"wrap-map.*-test"))

(comment

  (reset)
  (run-all-tests)

  :end)
