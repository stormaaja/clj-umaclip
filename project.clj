(defproject clj-umaclip "0.1.0"
  :description "Ultimate Clipboard"
  :url "https://github.com/stormaaja/clj-umaclip"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                  [compojure "1.5.1"]
                  [ring/ring-defaults "0.2.1"]
                  [ring "1.5.1"]
                  [ring.middleware.logger "0.5.0"]
                  [http-kit "2.2.0"]
                  [org.clojure/data.json "0.2.6"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler clj-umaclip.handler/app}
  :main ^:skip-aot clj-umaclip.handler
  :profiles {
    :uberjar {:aot :all}
    :test { :dependencies [[org.clojure/algo.generic "0.1.2"]]}
    :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
