(defproject machtest "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[bidi "2.0.14"]
                 [com.taoensso/timbre "4.7.4"]
                 [funcool/promesa "1.6.0"]
                 [hiccups "0.3.0"]
                 [macchiato/core "0.0.4"]
                 [macchiato/env "0.0.3"]
                 [mount "0.1.10"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-doo "0.1.7"]
            [lein-npm "0.6.2"]
            [lein-figwheel "0.5.8"]
            [lein-cljsbuild "1.1.4"]
            [org.clojure/clojurescript "1.9.293"]]
  :npm {:dependencies [[pg "6.1.0"]
                       [fibers "1.0.15"]
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
            :compiler     {:main          machtest.app
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
