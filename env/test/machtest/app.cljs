(ns machtest.app
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [machtest.core-test]))

(doo-tests 'machtest.core-test)


