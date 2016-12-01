(ns yada.profiles
  (:require
   [yada.spec :refer [validate]]
   [yada.context :as ctx]
   [manifold.deferred :as d]))

;; TODO: Would be better to avoid functions, copying the pattern in
;; yada.profile/error-renderer. This means that profiles can be fully
;; specified in config data. Functions can be replaced by defmultis
;; and keywords, retaining extensibility.
;; TODO list: nil-response-fn

(def profiles
  {:dev
   {:yada.profile/nil-response-fn
    (fn [ctx]
      (d/error-deferred
       (ex-info (format "No response function declared in resource for method %s" (ctx/method-token ctx))
                {:ring.response/status 500})))
    :yada.profile/validate-context? true
    :yada.profile/validate-set-cookie? true
    :yada.profile/interceptor-wrapper :debug
    :yada.profile/error-renderer :full}

   :prod
   {:yada.profile/nil-response-fn
    (fn [ctx]
      (d/error-deferred
       (ex-info "" {:ring.response/status 500})))
    :yada.profile/validate-context? false
    :yada.profile/validate-set-cookie? false
    :yada.profile/interceptor-wrapper :none
    :yada.profile/error-renderer :minimal}})
