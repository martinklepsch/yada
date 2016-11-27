;; Copyright © 2014-2016, JUXT LTD.

(ns yada.error
  (:require [yada.profile :as profile]))

(defn ^:interceptor handle-error [ctx]
  (let [e (:yada/error ctx)
        error-data (when (instance? clojure.lang.ExceptionInfo e) (ex-data e))
        status (when error-data (:ring.response/status error-data))]

    (when e
      (.printStackTrace e))

    (cond-> ctx
      status (assoc-in [:yada/response :ring.response/status] status)

      (yada.profile/reveal-exception-messages? ctx)
      (assoc-in [:yada/response :ring.response/body] (.getMessage ^Exception e))

      (not (yada.profile/reveal-exception-messages? ctx))
      (assoc-in [:yada/response :ring.response/body]
                (get-in status [(or status 500) :name])))))
