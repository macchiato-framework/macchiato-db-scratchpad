(ns machtest.routes
  (:require
    [bidi.bidi :as bidi]
    [hiccups.runtime]
    [macchiato.util.response :as r]
    [machtest.db :as db])
  (:require-macros
    [hiccups.core :refer [html]]))

(defn do-work [n]
  (let [t (.getTime (js/Date.))]
    (loop [now (.getTime (js/Date.))]
      (when (> n (- now t))
        (recur (.getTime (js/Date.)))))))

(defn home [req res raise]
  (db/detached-task
    (fn []
      (let [result (db/with-transaction "select * from names")
            sv     (->> (.-rows result)
                        (map #(aget % "name"))
                        (clojure.string/join ", "))]
        (-> (html
              [:html
               [:body
                [:h2 "Hello World!"]
                [:p "We found " sv " on the db"]]])
            (r/ok)
            (r/content-type "text/html")
            (res))))))

(defn delete [req res raise]
  (db/detached-task
    (fn []
      (let [result (db/with-transaction "delete from names")]
        (.log js/console "Deleted..." result)
        (-> (html
              [:html
               [:body
                [:h2 "Hello World!"]
                [:p "They should all be gone by now"]]])
            (r/ok)
            (r/content-type "text/html")
            (res))))))


(defn with-wait [req res raise]
  (db/detached-task
    (fn []
      (do-work 10000)
      (-> (html
            [:html
             [:body
              [:h2 "Hello World!"]
              [:p "I am done waiting"]]])
          (r/ok)
          (r/content-type "text/html")
          (res))
      )))

(defn not-found [req res raise]
  (-> (html
        [:html
         [:body
          [:h2 (:uri req) " was not found"]]])
      (r/not-found)
      (r/content-type "text/html")
      (res)))

(def routes
  ["/"
   [["" home]
    ["delete" delete]
    ["wait" with-wait]
    [true not-found]]])

(defn router [req res raise]
  (let [route (->> req :uri (bidi/match-route routes) :handler)]
    (route req res raise)))
