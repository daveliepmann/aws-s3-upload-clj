(defproject aws-s3-upload-clj "0.2.0-SNAPSHOT"
  :description "Clojure demo of uploading multiple files directly to AWS S3"
  :url "github.com/daveliepmann/aws-s3-upload-clj"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [ring-server "0.3.1"]
                 [hiccup "1.0.4"]
                 [compojure "1.1.8"]
                 [org.clojure/data.json "0.2.5"]
                 [lib-noir "0.8.3"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [cljs-ajax "0.2.4"]
                 [com.andrewmcveigh/cljs-time "0.1.5"]]
  :plugins [[lein-ring "0.8.10"]
            [lein-cljsbuild "1.0.2"]]
  :ring {:handler aws-s3-upload-clj.demo/app}
  :cljsbuild {:builds [{:id "main"
                        :source-paths ["src-cljs"],
                        :compiler {:pretty-print true,
                                   :output-to "resources/public/js/cljs.js",
                                   :optimizations :whitespace}}]}  
  :min-lein-version "2.0.0")
