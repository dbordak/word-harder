(defproject word-harder "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.1"]
                 [org.clojure/core.async "0.3.443"]
                 [re-com "2.1.0"]
                 [secretary "1.2.3"]
                 [compojure "1.6.0"]
                 [yogthos/config "0.8"]
                 [ring "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [http-kit "2.2.0"]
                 [com.taoensso/encore "2.91.1"]
                 [com.taoensso/sente "1.11.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.layerware/hugsql "0.4.7"]
                 [com.h2database/h2 "1.4.196"]
                 [com.andrewmcveigh/cljs-time "0.5.0"]
                 [clj-yaml "0.4.0"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler word-harder.handler/dev-handler}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :aliases {"dev" ["do" "clean"
                   ["pdo" ["figwheel" "dev"]]]
            "build" ["do" "clean"
                     ["cljsbuild" "once" "min"]]
            "migrate" ["run" "-m" "user/migrate"]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.4"]
                   [figwheel-sidecar "0.5.14"]
                   [com.cemerick/piggieback "0.2.2"]
                   [re-frisk "0.5.0"]]

    :plugins      [[lein-figwheel "0.5.14"]
                   [lein-pdo "0.1.1"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "word-harder.core/mount-root"}
     :compiler     {:main                 word-harder.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           re-frisk.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :jar true
     :compiler     {:main            word-harder.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}


    ]}

  :main word-harder.server

  :aot [word-harder.server]

  :uberjar-name "word-harder.jar"

  :prep-tasks [["cljsbuild" "once" "min"] "compile"]
  )
