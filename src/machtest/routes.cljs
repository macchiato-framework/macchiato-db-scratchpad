(ns machtest.routes
  (:require
    [bidi.bidi :as bidi]
    [hiccups.runtime]
    [macchiato.futures.core :refer [task detached-task]]
    [macchiato.util.response :as r]
    [machtest.db :as db])
  (:require-macros
    [hiccups.core :refer [html]]))

(defn do-work [n]
  (let [t (.getTime (js/Date.))]
    (loop [now (.getTime (js/Date.))]
      (when (> n (- now t))
        (recur (.getTime (js/Date.)))))))


(defn add-new-users []
  (db/with-transaction
    (fn [query]
      (query "insert into names (name, age) values ($1::text, $2)" "joeBob" 21)
      (query "insert into names (name, age) values ($1::text, $2)" "jack" 25)
      (query "insert into names (name) values ($1)" "Unnamed")
      "Added everyone")))


(defn home [req res raise]
  (detached-task
    (fn []
      (let [result     (db/single-query "select * from names")
            jack-count (->
                         (db/single-query "select count(*) from names where name = $1" "jack")
                         (aget "rows")
                         first
                         (aget "count"))
            sv         (->> (.-rows result)
                            (map #(aget % "name"))
                            (clojure.string/join ", "))]
        (-> (html
              [:html
               [:body
                [:h2 "Hello World!"]
                [:p "We found " sv " on the db"]
                [:p "A grand total of " jack-count " were named jack. The type returned was " (type jack-count)]
                [:ul
                 [:li [:a {:href "/create"} "Add new users here"]]
                 [:li [:a {:href "/delete"} "Delete them all here"]]]]]
              )
            (r/ok)
            (r/content-type "text/html")
            (res))))))


(defn create [req res raise]
  (detached-task
    (fn []
      (let [result (add-new-users)]
        (.log js/console "Result:" result)
        (-> (html
              [:html
               [:body
                [:h2 "Hello World!"]
                [:p result]
                [:ul
                 [:li [:a {:href "/"} "Check on root"]]
                 [:li [:a {:href "/delete"} "Delete them all here"]]]]])
            (r/ok)
            (r/content-type "text/html")
            (res))))))

(defn delete [req res raise]
  (detached-task
    (fn []
      (let [result (db/single-query "delete from names")
            total  (aget result "rowCount")]
        (.log js/console "Deleted..." result)
        (-> (html
              [:html
               [:body
                [:h2 "Hello World!"]
                [:p "Deleted " total " users. They should all be gone by now"]
                [:ul
                 [:li [:a {:href "/"} "Check on root"]]
                 [:li [:a {:href "/create"} "Add new users here"]]]]])
            (r/ok)
            (r/content-type "text/html")
            (res))))))


(defn with-wait [req res raise]
  (detached-task
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
    ["create" create]
    ["delete" delete]
    ["wait" with-wait]
    [true not-found]]])

(defn router [req res raise]
  (let [route (->> req :uri (bidi/match-route routes) :handler)]
    (route req res raise)))
