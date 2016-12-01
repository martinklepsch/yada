;; Copyright © 2014-2016, JUXT LTD.

(ns yada.handler
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [manifold.deferred :as d]
            [yada.error :as error]
            [yada.method :refer [perform-method]]
            [yada.context :as ctx]
            [yada.response :as response]
            [yada.spec :refer [validate]]
            [yada.status :refer [status]]
            [yada.interceptor :refer [transform-interceptor-chain]]
            [yada.profile :as profile]))

(s/def :yada.handler/interceptor
  (s/with-gen fn? #(gen/return identity)))

(s/def :yada.handler/interceptor-chain
  (s/coll-of :yada.handler/interceptor))

(s/def :yada/handler
  (s/keys :req [:yada/resource
                :yada.handler/interceptor-chain
                :yada/profile]
          :opt [:yada.handler/error-interceptor-chain]))

(defn ^:interceptor terminate [ctx]
  (println "TERMINATING, ctx is " ctx)
  (->
   (response/->ring-response (ctx/response ctx))
   ;; TODO: For modularity, do this in another cookies interceptor
   (update :headers merge (some-> (:yada.response/cookies (ctx/response ctx)) yada.cookies/->headers))))

(defn apply-interceptors [ctx]
  ;; Have to validate the ctx here, it's the last chance.
  ;; Can't do this in prod
  (when (and (profile/validate-context? ctx))
    (validate ctx :yada/context "Context is not valid"))

  (let [chain (:yada.handler/interceptor-chain ctx)]
    (->
     (apply d/chain ctx (transform-interceptor-chain ctx (concat chain [terminate])))
     (d/catch Exception
         (fn [e]
           (println "ERROR catching")
           (->
            (apply d/chain
                   (assoc-in ctx [:yada/response :yada/error :yada.error/exception] e)
                   (or (:yada.handler/error-interceptor-chain ctx)
                       [error/handle-error terminate]))
            (d/catch Exception
                (fn [e] ;; TODO: Error in error chain, log the original error and politely return a 500
                  (println "Error in error handling")
                  e))))))))

(defrecord Handler []
  clojure.lang.IFn
  (invoke [this req]
    (apply-interceptors (ctx/new-context (into {:ring/request req} this)))))

(defn new-handler [model]
  (map->Handler model))

(defn accept-request [^Handler handler req]
  (.invoke handler req))
