;; Copyright © 2014-2016, JUXT LTD.

(ns ^{:doc "A profile defines what yada does in certain
    cases. Profiles typically reflect operating environments, mapping
    onto runtime profiles such as :dev and :prod"} yada.profile
  (:require
   [clojure.spec :as s]
   [yada.spec :refer [validate]]))

(s/def :yada.profile/nil-response-fn fn?)
(s/def :yada.profile/validate-context? boolean?)
(s/def :yada.profile/validate-set-cookie? boolean?)
(s/def :yada.profile/interceptor-wrapper fn?)
(s/def :yada.profile/error-renderer keyword?)

(s/def :yada/profile (s/keys :req [:yada.profile/nil-response-fn
                                   :yada.profile/error-renderer
                                   :yada.profile/validate-context?
                                   :yada.profile/validate-set-cookie?]
                             :opt [:yada.profile/interceptor-wrapper]))

(defn nil-response-fn [ctx]
  (when-let [f (get-in ctx [:yada/profile :yada.profile/nil-response-fn])]
    (f ctx)))

(defn validate-context? [ctx]
  (get-in ctx [:yada/profile :yada.profile/validate-context?]))

(defn validate-set-cookie? [ctx]
  (get-in ctx [:yada/profile :yada.profile/validate-set-cookie?]))

(defn interceptor-wrapper [ctx]
  (get-in ctx [:yada/profile :yada.profile/interceptor-wrapper]))

(defn error-renderer [ctx]
  (get-in ctx [:yada/profile :yada.profile/error-renderer]))
