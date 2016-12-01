;; Copyright © 2014-2016, JUXT LTD.

(ns ^{:doc "Functions that encapsulate knowledge of the yada request context structure. This layer of indirection allows the structure of the request context to evolve without continually breaking code that uses it."}
    yada.context
  (:require
   [clojure.string :as str]
   [clojure.spec :as s]
   clojure.string
   yada.resource
   [yada.profile :as p]
   [ring.core.spec :as rs]
   [yada.spec :refer [validate]]
   yada.cookies
   [yada.response :as response]
   [ring.middleware.cookies :refer [cookies-request cookies-response]])
  (:import [yada.response Response]))

(s/def :yada/request (s/keys :req []
                             :opt [:yada.request/authentication]))

(defn add-status [ctx status]
  (assoc-in ctx [:yada/response :yada.response/status] status))

(defn add-body [ctx body]
  (assoc-in ctx [:yada/response :yada.response/body] body))

(defn add-header [ctx n v]
  (assoc-in ctx [:yada/response :yada.response/headers n] v))

(s/def :yada/context
  (s/keys :req [:yada/resource
                :ring/request ; original Ring request
                :yada/request ; request extras, like cookies
                :yada/response
                :yada/method-token
                :yada/profile]))

(defn new-context "Create a request context"
  [init-ctx]
  (let [req (:ring/request init-ctx)]
    (merge
     (when req
       {:yada/method-token (-> req :request-method name str/upper-case)
        :yada/request {:yada.request/cookies* (delay (:cookies (cookies-request req)))}})
     {:yada/response (response/new-response)}
     init-ctx)))

(defn method-token [ctx]
  (-> ctx :yada/method-token))

(defn lookup-method [ctx]
  (yada.resource/lookup-method
   (:yada/resource ctx)
   (:yada/method-token ctx)))

(defn authentication-schemes [ctx]
  (get-in ctx [:yada/resource :yada.resource/authentication]))

(defn response [ctx]
  (:yada/response ctx))

(defn set-status [response status]
  (assert (instance? Response response) "Response parameter is not a response record")
  (assoc-in response [:ring.response/status] status))

(defn set-body [response body]
  (assert (instance? Response response) "Response parameter is not a response record")
  (assoc-in response [:ring.response/body] body))

(defn authentication [ctx]
  (get-in ctx [:yada/request :yada.request/authentication]))

(defn claims [ctx]
  (apply merge (map :yada.authentication/claims (authentication ctx))))

(defn authorization-predicate [ctx]
  (get-in ctx [:yada/resource :yada.resource/authorized?]))

(defn error [ctx]
  (get-in ctx [:yada/response :yada/error]))
