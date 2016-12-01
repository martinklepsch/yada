;; Copyright © 2014-2016, JUXT LTD.

(ns yada.response
  (:require [clojure.spec :as s]))

(s/def :yada/response (s/keys :req [:yada.response/headers]
                              :opt [:yada.response/status
                                    :yada.response/body
                                    :yada.response/cookies
                                    :yada.response/error]))

(defrecord
    ^{:doc "This record is used as an escape mechanism users to
    return it in method responses. Doing this indicates to yada that
    the user knows what they are doing and want fine grained control
    over the response, perhaps setting cookies, response headers and
    so on."}
    Response [])

(defn new-response []
  (map->Response {:yada.response/headers {}}))
