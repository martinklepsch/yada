;; Copyright © 2014-2016, JUXT LTD.

(ns yada.response
  (:require [clojure.spec :as s]
            [clojure.tools.logging :refer :all]))

(s/def :yada/response
  (s/keys :req [:yada.response/headers]
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

(defn ->ring-response [response]
  (when-not (:yada.response/status response)
    (warnf "No :yada.response/status set, status is defaulting to 500"))

  (merge
   {:status (or (:yada.response/status response) 500)
    :headers (or (:yada.response/headers response) {})}
   (when-let [body (:yada.response/body response)]
     {:body body})))
