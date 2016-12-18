(ns machtest.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [machtest.core-test]))

(doo-tests 'machtest.core-test)