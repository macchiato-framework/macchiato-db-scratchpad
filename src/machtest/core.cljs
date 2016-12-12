(ns machtest.core
  (:require
    [machtest.config :refer [env]]
    [machtest.middleware :refer [wrap-defaults]]
    [machtest.routes :refer [router]]
    [macchiato.env :as config]
    [macchiato.http :refer [handler]]
    [macchiato.session.memory :as mem]
    [mount.core :as mount :refer [defstate]]
    [taoensso.timbre :refer-macros [log trace debug info warn error fatal]]))


(defstate http :start (js/require "http"))

(defn app []
  (mount/start)
  (let [host (or (:host env) "127.0.0.1")
        port (or (some-> env :port js/parseInt) 3000)]
    (-> @http
        (.createServer
          (handler
            (wrap-defaults router)
            {:cookies {:signed? true}
             :session {:store (mem/memory-store)}}))
        (.listen port host #(info "machtest started on" host ":" port)))))

(defn start-workers [os cluster]
  (dotimes [_ (-> os .cpus .-length)]
    (.fork cluster))
  (.on cluster "exit"
       (fn [worker code signal]
         (info "worker terminated" (-> worker .-process .-pid)))))

(defn main [& args]
  (let [os      (js/require "os")
        cluster (js/require "cluster")]
    (if (.-isMaster cluster)
      (start-workers os cluster)
      (app))))
