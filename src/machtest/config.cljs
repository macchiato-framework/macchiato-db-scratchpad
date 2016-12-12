(ns machtest.config
  (:require [macchiato.env :as me]
            [mount.core :as mount :refer [defstate]]))

(defstate env :start (me/env))
