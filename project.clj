(defproject machtest "0.3.0-SNAPSHOT"
  :description "Scratchpad project to test implementation approaches for macchiato"
  :url "https://github.com/macchiato-framework/macchiato-db-scratchpad"
  :dependencies [[bidi "2.0.16"]
                 [com.taoensso/timbre "4.8.0"]
                 [environ "1.1.0"]
                 [hiccups "0.3.0"]
                 [macchiato/core "0.1.7"]
                 [macchiato/env "0.0.5"]
                 [macchiato/async "0.0.4-SNAPSHOT"]
                 [macchiato/sql "0.0.2"]
                 [mount "0.1.11"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.495"]
                 [prismatic/schema "1.1.3"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-doo "0.1.7"]
            [lein-environ "1.1.0"]
            [lein-npm "0.6.2"]
            [lein-figwheel "0.5.9"]
            [lein-cljsbuild "1.1.4"]
            [org.clojure/clojurescript "1.9.495"]]
  :npm {:dependencies [[pg "6.1.0"]
                       [source-map-support "0.4.6"]
                       [ws "1.1.1"]]}
  :source-paths ["src" "target/classes"]
  :clean-targets ["target"]
  :target-path "target"
  :profiles
  {:dev
   {:cljsbuild
                  {:builds {:dev
                            {:source-paths ["env/dev" "src"]
                             :figwheel     true
                             :compiler     {:main          machtest.app
                                            :output-to     "target/out/machtest.js"
                                            :output-dir    "target/out"
                                            :target        :nodejs
                                            :optimizations :none
                                            :pretty-print  true
                                            :source-map    true}}}}
    :env          {:db-user     "machtest"
                   :db-password "testdb"
                   :db-name     "machtest_dev"
                   :db-host     "localhost"}

    :figwheel
                  {:http-server-root "public"
                   :nrepl-port       7000
                   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

    :dependencies [[com.cemerick/piggieback "0.2.1"]]
    :source-paths ["env/dev"]
    :repl-options {:init-ns user}}
   :test
   {:cljsbuild
         {:builds
          {:test
           {:source-paths ["env/test" "src" "test"]
            :compiler     {:main          machtest.runner
                           :output-to     "target/test/machtest.js"
                           :target        :nodejs
                           :optimizations :none
                           :source-map    true
                           :pretty-print  true}}}}
    :doo {:build "test"}}
   :release
   {:cljsbuild
    {:builds
     {:release
      {:source-paths ["env/prod" "src"]
       :compiler     {:main          machtest.app
                      :output-to     "target/release/machtest.js"
                      :target        :nodejs
                      :optimizations :simple
                      :pretty-print  false}}}}}}
  :aliases
  {"build"   ["do"
              ["clean"]
              ["npm" "install"]
              ["figwheel" "dev"]]
   "package" ["do"
              ["clean"]
              ["npm" "install"]
              ["npm" "init" "-y"]
              ["with-profile" "release" "cljsbuild" "once"]]
   "test"    ["do"
              ["npm" "install"]
              ["with-profile" "test" "doo" "node"]]})
