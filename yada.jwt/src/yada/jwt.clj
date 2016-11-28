;; Copyright © 2014-2016, JUXT LTD.

(ns yada.jwt
  (:require [yada.authentication :as a]
            [yada.cookies :as cookies]
            [buddy.sign.jwt :as jwt]))

(defn secret [ctx]
  (:yada.jwt/secret ctx))

(defmethod a/authenticate-with-scheme :jwt [scheme ctx]
  (some-> (cookies/cookies ctx)
          (get "session")
          :value
          (jwt/unsign (secret ctx))))
