;; Copyright © 2014-2016, JUXT LTD.

(ns yada.jwt
  (:require [yada.authentication :as a]
            [yada.cookies :as cookies]
            [clojure.spec :as s]
            [buddy.sign.jwt :as jwt]
            [yada.spec :refer [validate]]))

(defmethod a/authenticate-with-scheme :jwt [scheme ctx]
  (validate scheme
            (s/keys :req [:yada.resource.authentication/scheme
                          :yada.jwt/secret])
            "Scheme doesn't match expected for JWT")
  (some-> (cookies/cookies ctx)
          (get "session")
          :value
          (jwt/unsign (:yada.jwt/secret scheme))))
