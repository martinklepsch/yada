;; Copyright © 2014-2016, JUXT LTD.

(ns yada.authorization
  (:require [yada.context :as ctx]))

(defn ^:interceptor authorize [ctx]
  (println "standard yada authorization")
  (let [claims (ctx/authenticated-claims ctx)]

    (if
      (= claims {:user "malcolm"})
      ctx ; allow to proceed

      (if claims
        (throw (ex-info "" {:ring.response/status 403}))
        (throw (ex-info "" {:ring.response/status 401}))))))
