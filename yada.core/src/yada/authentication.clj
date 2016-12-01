;; Copyright © 2014-2016, JUXT LTD.

(ns yada.authentication
  (:require [yada.context :as ctx]))

(defmulti authenticate-with-scheme "" (fn [scheme ctx] (:yada.resource.authentication/scheme scheme)))

(defmethod authenticate-with-scheme :default [scheme ctx]
  nil)

(defn ^:interceptor authenticate [ctx]
  (assoc-in ctx [:yada/request :yada.request/authentication]
            (remove nil?
                    (for [scheme (ctx/authentication-schemes ctx)]
                      (merge
                       {:yada.authentication/claims
                        (authenticate-with-scheme scheme ctx)}
                       {:yada.request/scheme (:yada.resource.authentication/scheme scheme)
                        }
                       (when (:yada.resource/realm scheme)
                         {:yada.request/realm (:yada.resource/realm scheme)}))))))
