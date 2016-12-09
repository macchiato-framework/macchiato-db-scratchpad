(ns machtest.db)


(def config {:user              "machtest"
             :database          "machtest_dev"
             :password          "testdb"
             :host              "localhost"
             :port              5432
             :max               10
             :idleTimeoutMillis 3000
             })

(def pg (js/require "pg"))

(def Future (js/require "fibers/future"))
(def pool-future (.wrap Future (pg.Pool. (clj->js config))))
(defn run-future [query-string]
  (-> (.queryFuture pool-future "select * from names")
      .wait))

(defn detached-task [f] (->> f (.task Future) .detach))
