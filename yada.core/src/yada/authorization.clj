;; Copyright © 2014-2016, JUXT LTD.

(ns yada.authorization
  (:require [yada.context :as ctx]))

(defn ^:interceptor authorize [ctx]
  (let [authz (ctx/authorization-predicate ctx)]
    (if authz
      (if (authz ctx)
        ctx
        (if (ctx/claims ctx)
          (throw (ex-info "" {:ring.response/status 403}))
          (throw (ex-info "" {:ring.response/status 401}))))

      ;; TODO: There is no authz function
      ;; Should we demand one?
      ;; If there's an authentication scheme in effect, they yes!
      (do
        (println "No authz predicate")
        ctx))))
