(ns machtest.db
  (:require [macchiato.futures :refer [wait wrap-future]]
            [mount.core :as mount :refer [defstate]]
            [machtest.config :refer [env]]))


(def config {:user              "machtest"
             :database          "machtest_dev"
             :password          "testdb"
             :host              "localhost"
             :port              5432
             :max               20
             :idleTimeoutMillis 10000})

; Need to still load env settings into the environment
#_{:user              (:db-user env)
   :database          (:db-name env)
   :password          (:db-password env)
   :host              (:db-host env)
   :port              (or (:db-port env) 5432)
   :max               (or (:db-max-connections env) 20)
   :idleTimeoutMillis (or (:db-idle-timeout env) 10000)}


(def pg (js/require "pg"))

(defstate ^:dynamic db-pool
  :start (->>
           (doto (pg.Pool. (clj->js config))
             (.on "error" #(.error js/console "Conn error" %1 %2))
             (.on "connect" #(.log js/console "Connection!"))
             (.on "acquire" #(.log js/console "Acquired!")))
           wrap-future)
  :end (.end db-pool))


(defn query-no-pool [query-string]
  (.wait (.query @db-pool query-string)))


(defn with-transaction
  "Will initialize a client from the database pool, obtain a query-fn,
  and then invoke the function it receives with the query-fn as its
  argument. Does transaction handling.

  The function will be executed in a transaction, which will be rolled
  back in case of an exception. Any exception caught will be re-thrown."
  [f]
  (let [client   (wait (.connect @db-pool))
        ; Wrapping the entire client causes an issue on repeated requests,
        ; because it loses some references. Looks like what we get back from
        ; the wrapping is a proxy. It's probably meant for wrapping modules
        ; and not instances, but wrapping a single function works well.
        q-future (wrap-future client.query)
        query    (fn [qs & rest]
                   (wait (.call q-future client qs (clj->js rest))))]
    (try
      (query "BEGIN")
      (let [r (f query)]
        (query "COMMIT")
        r)
      (catch js/Error e
        (query "ROLLBACK")
        (throw e))
      (finally
        (.release client)))))

(defn single-query
  [query-string]
  (with-transaction
    #(% query-string)))