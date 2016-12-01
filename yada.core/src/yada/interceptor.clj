(ns yada.interceptor
  (:require [yada.spec :refer [validate]]
            [yada.profile :as profile]))

(defmulti transform-interceptor-chain ""
  (fn [ctx chain]
    (profile/interceptor-wrapper ctx)))

(defmethod transform-interceptor-chain :none [ctx chain]
  chain)

(defn debug-interceptor-wrapper [i]
  (fn [ctx]
    (println "HERE: i is " i ",ctx is " ctx)
    (validate ctx :yada/context (format "Context not valid on entering interceptor: %s" i))
    (i ctx)))

(defmethod transform-interceptor-chain :debug [ctx chain]
  (map debug-interceptor-wrapper chain))
