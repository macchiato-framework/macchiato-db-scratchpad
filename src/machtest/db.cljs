(ns machtest.db
  (:require [machtest.config :refer [env]]
            [macchiato.async.futures :refer [wait wrap-future]]
            [mount.core :as mount :refer [defstate]]))


;;;;;;;;;;;;;;;;;;;;;
;;;; Requires
;;;;;;;;;;;;;;;;;;;;;

(def pg (js/require "pg"))
(def pg-types (.-types pg))



;;;;;;;;;;;;;;;;;;;;;
;;;; Configuration
;;;;;;;;;;;;;;;;;;;;;


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



;; Going to configure type coercion using node-pg-types (https://github.com/brianc/node-pg-types)
;;
;; This depends on Postgres' data type ids: https://doxygen.postgresql.org/include_2catalog_2pg__type_8h.html

(def TYPANALYZE 20)
(.setTypeParser
  pg-types
  TYPANALYZE
  (fn [val]
    (let [as-float (js/parseFloat val)]
      ;; The reason I need to do this now is that querying for a "count" is
      ;; returning a string. Looking at the metadata it's a data type of 20,
      ;; which is "analyze".
      ;;
      ;; https://doxygen.postgresql.org/include_2catalog_2pg__type_8h.html#ab5abe002baf3cb0ccf3f98008c72ca8a
      ;;
      ;; Javascript numeric parsing is wonky, though.
      ;;
      ;; (js/parseInt "8Ricardo") is 8.
      ;; (js/parseFloat "8.5.9Tomato") is 8.5
      ;;
      ;; ಠ_ಠ
      ;;
      ;; Hopefully we won't get back any TYPANALYZE result that starts with a
      ;; number but is not actually numeric. Requires more experimentation.
      (cond
        (nil? val) nil
        (js/isNaN as-float) val
        (number? as-float) as-float
        :default val))))

(def TYPUUIDOID 2950)
(.setTypeParser
  pg-types
  TYPUUIDOID
  #(cljs.core/uuid %))


;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; Functions and state
;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defstate ^:dynamic db-pool
          :start (wrap-future
                   (doto (pg.Pool. (clj->js config))
                     (.on "error" #(.error js/console "Conn error" %1 %2))
                     (.on "connect" #(.log js/console "Connection!"))
                     (.on "acquire" #(.log js/console "Acquired!"))))
          :end (.end @db-pool))


(defn query-no-pool [query-string]
  (.wait (.query @db-pool query-string)))


(defn with-transaction
  "Will initialize a client from the database pool, obtain a query-fn,
  and then invoke the function it receives with the query-fn as its
  argument.

  Every call to the query function will be waited on, to ensure we are
  executing them sequentially.

  The functions will be executed in a transaction, which will be rolled
  back in case of an exception. Any exception caught will be re-thrown.

  Returns the result of releasing the client."
  [f]
  (let [client   (wait (.connect @db-pool))
        ;; Wrapping the entire client causes an issue on repeated requests,
        ;; because it loses some references. Looks like what we get back from
        ;; the wrapping is a proxy. It's probably meant for wrapping modules
        ;; and not instances, but wrapping a single function works well.
        ;;
        ;; I'm thinking this might be doable in a single step with a
        ;; function called `wrap-future-method`, which returns not
        ;; just a future, but a function that invokes the future on a specific
        ;; object. Similar to the
        ;;
        ;;   (.call q-future client qs (clj->js rest))
        ;;
        ;; invocation below. I'd probably need a to pass both the object to
        ;; invoke it on and the method being future-ified, though, since
        ;; I don't think there's a way to get the object a function originally
        ;; came from.
        ;;
        ;; For now I'm keeping the semantics transparent.
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
  "Runs a single query in a transaction."
  [& rest]
  (with-transaction
    #(apply % rest)))