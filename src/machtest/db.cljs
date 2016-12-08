(ns machtest.db
  (:require [promesa.core :as p :refer-macros [alet]]))


(def config {:user              "machtest"
             :database          "machtest_dev"
             :password          "testdb"
             :host              "localhost"
             :port              5432
             :max               10
             :idleTimeoutMillis 3000
             })

(def pg (js/require "pg"))

(def pool (pg.Pool. (clj->js config)))

(defn run-promise [query-string]
  (p/promise
    (fn [resolve reject]
      (.query
        pool
        query-string
        (fn [err res]
          (if err
            (reject err)
            (resolve res)))))))

(defn run-promise-with-client [query-string]
  (p/promise
    (fn [resolve reject]
      (.connect
        pool
        (fn [err client done]
          (if err
            (reject "Error fetching client" err)
            (.query
              client query-string
              (fn [err r]
                (done)                                    ; Release client
                (if err
                  (reject err)
                  (resolve r))))))))))



(def Future (js/require "fibers/future"))
(def pool-future (.wrap Future (pg.Pool. (clj->js config))))
(defn run-future [query-string]
  (-> (.queryFuture pool-future "select * from names")
      .wait))

(defn detached-task [f] (->> f (.task Future) .detach))
