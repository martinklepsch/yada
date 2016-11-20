;; Copyright © 2014-2016, JUXT LTD.

(defproject yada.core "2.0.0-alpha1"
  :description "The yada core"
  :url "http://github.com/juxt/yada"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :exclusions [org.clojure/clojure]

  :plugins [[lein-modules "0.3.11"]]

  :dependencies
  [[byte-streams "0.2.2"]
   [manifold "0.1.6-alpha3"]
   [org.clojure/data.codec "0.1.0"]
   [org.clojure/test.check "0.9.0"]
   [org.clojure/tools.logging "0.3.1"]
   [potemkin "0.4.3"]
   [ring-spec "0.0.2"]]

;;  :pedantic? :abort

  :global-vars {*warn-on-reflection* true}

  :repl-options {:init-ns user
                 :welcome (println "Type (dev) to start")}

  :profiles
  {:dev {:jvm-opts ["-Xms1g" "-Xmx1g"
                    "-server"
                    "-Dio.netty.leakDetectionLevel=paranoid"]

;;         :pedantic? :abort

         :dependencies
         [[org.clojure/clojure :version]

          [ch.qos.logback/logback-classic "1.1.5"
           :exclusions [org.slf4j/slf4j-api]]
          [org.slf4j/jul-to-slf4j "1.7.18"]
          [org.slf4j/jcl-over-slf4j "1.7.18"]
          [org.slf4j/log4j-over-slf4j "1.7.18"]

          [aleph "0.4.2-alpha8"]]}})
