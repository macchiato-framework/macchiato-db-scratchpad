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


(defn single-query [query-string]
  (.wait (.query @db-pool query-string)))


(defn with-transaction [query-string]
  (try
    (let [client   (wait (.connect @db-pool))
          ; Wrapping the entire client causes an issue on repeated requests,
          ; because it loses some references. Looks like what we get back from
          ; the wrapping is a proxy. It's probably meant for wrapping modules
          ; and not instances, but wrapping a single function works well.
          q-future (wrap-future client.query)
          query    (fn [qs & rest]
                     (wait (.call q-future client qs (clj->js rest))))
          _        (query "BEGIN")
          _        (query "insert into names (name) values ('the new guy')")
          _        (query "insert into names (name, age) values ($1::text, $2)" "joeBob" 21)
          result   (query query-string)
          _        (query "ROLLBACK")]
      ; To Review: If we get an exception, I' expect the client to not be released
      ; That's another reason why it would be real nice if we could just do this
      ; inside a macro.
      (.release client)
      (.log js/console "Released")
      result)
    (catch js/Error e
      (.error js/console "Caught" e)
      [])))
