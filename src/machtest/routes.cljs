(ns machtest.routes
  (:require
    [bidi.bidi :as bidi]
    [hiccups.runtime]
    [macchiato.util.response :as r]
    [promesa.core :as p :refer-macros [alet]])
  (:require-macros
    [hiccups.core :refer [html]]))

(defn home [req res raise]
  #_(machtest.db/detached-task
      (fn []
        (let [result (machtest.db/run-future "select * from names")
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
              (res)))))

  (alet [result (p/await (machtest.db/run-promise "select * from names"))
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
        (res))))

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
    [true not-found]]])

(defn router [req res raise]
  (let [route (->> req :uri (bidi/match-route routes) :handler)]
    (route req res raise)))
