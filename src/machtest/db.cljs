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

(defn query [query-string]
  (.wait (.queryFuture pool-future query-string)))

(defn with-transaction [query-string]
  (.log js/console "Entered with-transaction" query-string)
  (try
    (let [client   (.wait (.connectFuture pool-future))
          ; Wrapping the entire client causes an issue on repeated requests,
          ; because it loses some references
          q-future (.wrap Future client.query)
          query    #(.wait (.call q-future client %))
          _        (query "BEGIN")
          _        (query "insert into names (name) values ('the new guy')")
          result   (query query-string)
          _        (query "ROLLBACK")
          ]
      (.log js/console result)
      (.release client)
      (.log js/console "Released")
      result)
    (catch js/Error e
      (.error js/console "Caught" e)
      [])))

#_(defn with-transaction [query-string]
    (.log js/console "Entered with-transaction")
    (let [non-wrapped (pg.Client. (clj->js config))
          client      (.wrap Future non-wrapped)
          _           (.log js/console "To transaction...")
          tr          (.wait (.queryFuture client "BEGIN"))
          _           (.log js/console tr)
          result      (.wait (.queryFuture client query-string))
          _           (.log js/console (.wait (.queryFuture client "ROLLBACK")))
          ]
      (.wait (.releaseFuture client))
      result))

(defn detached-task [f] (->> f (.task Future) .detach))

#_(defn detached-task [f]
    (.future f))
