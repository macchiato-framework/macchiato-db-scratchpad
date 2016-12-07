(ns machtest.core-test
  (:require
    [cljs.test :refer-macros [is are deftest testing use-fixtures]]
    [machtest.core]))

(deftest test-core
  (is (= true true)))


