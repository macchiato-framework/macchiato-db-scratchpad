(ns machtest.db)


(def config {:user              "machtest"
             :database          "machtest_dev"
             :password          "testdb"
             :host              "localhost"
             :port              5432
             :max               20
             :idleTimeoutMillis 10000
             })

(def pg (js/require "pg"))

(def Future (js/require "fibers/future"))
(def pool (pg.Pool. (clj->js config)))

(.on pool "error" #(.error js/console "Conn error" %1 %2))
(.on pool "connect" #(.log js/console "Connection!"))
(.on pool "acquire" #(.log js/console "Acquired!"))


(def pool-future (.wrap Future pool))

(defn single-query [query-string]
  (.wait (.queryFuture pool-future query-string)))

(defn with-transaction [query-string]
  (.log js/console "Entered with-transaction" query-string)
  (try
    (let [client   (.wait (.connectFuture pool-future))
          ; Wrapping the entire client causes an issue on repeated requests,
          ; because it loses some references. Looks like what we get back from
          ; the wrapping is a proxy. It's probably meant for wrapping modules
          ; and not instances, but wrapping a single function works well.
          q-future (.wrap Future client.query)
          query    (fn [qs & rest]
                     (.wait (.call q-future client qs (clj->js rest))))
          _        (query "BEGIN")
          _        (query "insert into names (name) values ('the new guy')")
          _        (query "insert into names (name, age) values ($1::text, $2)" "joeBob" 21)
          result   (query query-string)
          _        (query "ROLLBACK")
          ]
      ; (.log js/console result)
      (.release client)
      (.log js/console "Released")
      result)
    (catch js/Error e
      (.error js/console "Caught" e)
      [])))

(defn detached-task [f] (->> f (.task Future) .detach))

#_(defn detached-task [f]
    (.future f))
