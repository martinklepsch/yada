;; Copyright © 2014-2016, JUXT LTD.

(ns yada.methods.get
  (:require
   [manifold.deferred :as d]
   [yada.method :refer [http-method]]
   [yada.profile :as profile]
   [yada.spec :refer [validate]]
   yada.context
   [yada.context :as ctx])
  (:import
   (yada.context Response)))

(defprotocol ResultHandler
  "The ResultHandler protocol is intended to allow for user extension and override"

  (handle-result [_ ctx]
    "Take the result of a yada.method/response function and modify the
  context accordingly - executed in a manifold.deferred chain so
  return of deferreds are permitted"))

(extend-protocol ResultHandler

  Response
  (handle-result [response ctx]
    (assoc ctx :ring/response response))

  Object
  (handle-result [o ctx]
    (-> ctx
        (ctx/add-status 200)
        (ctx/add-body o)))

  nil
  (handle-result [_ ctx]
    (d/error-deferred (ex-info "" {:ring.response/status 404})))

  ;; Trampoline behavior
  clojure.lang.Fn
  (handle-result [f ctx]
    (handle-result (f ctx) ctx)))

(defn wrapper [ctx]
  (let [method (yada.context/lookup-method ctx)
        response-fn (:yada.resource/response method)]

    (cond
      (nil? method) (d/error-deferred (ex-info "No matching method in resource" {:ring.response/status 405}))
      (nil? response-fn) (profile/nil-response-fn ctx)
      :otherwise
      (d/chain

       ;; Call the response function
       (try
         (->
          (response-fn ctx))
         (catch Exception e
           (d/error-deferred e)))

       ;; Handle result
       #(handle-result % ctx)))))

(defmethod http-method "GET"
  [token]
  {:yada.method/safe? true
   :yada.method/wrapper wrapper})
