(ns macchiato.futures)

(def Future (js/require "fibers/future"))

(defn wrap-future
  "Wraps an object or function in a future. Notice that by default we won't use
  any suffix."
  ([o]
   (wrap-future o false "" false))
  ([o multi? suffix stop?]
   (.wrap Future o multi? suffix stop?)))

(defn detached-task
  "Runs a function as a detached task."
  [f]
  (->> f (.task Future) .detach))

(defn task
  "Runs a function as a task."
  [f]
  (.task Future f))


(defn wait
  [f]
  (.wait f))