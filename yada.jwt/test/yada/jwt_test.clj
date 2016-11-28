;; Copyright © 2014-2016, JUXT LTD.

(ns yada.jwt-test
  (:require [clojure.test :refer :all]
            [clojure.spec.gen :as gen]
            [yada.authentication :as authn]
            [yada.authorization :as authz]
            [yada.handler :refer [new-handler accept-request]]
            [yada.profiles :refer [profiles]]
            [yada.cookies :as cookies]
            [yada.resource :refer [new-resource]]
            [buddy.sign.jwt :as jwt]
            [yada.test-util :refer [new-request]]
            yada.jwt
            [yada.method :as method]
            [yada.context :as ctx]))

(jwt/sign {:foo 1} "secret")

(deftest authentication []
  )


(let [secret (gen/generate (gen/gen-for-pred string?))]

  (let [response
        @(accept-request
          (new-handler {:yada/resource
                        (new-resource
                         {:yada.resource/methods
                          {"POST"
                           {:yada.resource/response
                            (fn [ctx]
                              (-> (:yada/response ctx)
                                  (cookies/set-cookie
                                   ctx "session"
                                   {:yada.cookie/value (jwt/sign {:user "malcolm"} secret)})))}}})
                        :yada.handler/interceptor-chain [method/perform-method]
                        :yada/profile (profiles :dev)})
          (new-request :post "http://localhost"))

        cookie (first (get-in response [:headers "Set-Cookie"]))

        response2
        @(accept-request
          (new-handler {:yada.jwt/secret secret
                        :yada/resource
                        (new-resource
                         {:yada.resource/authentication :jwt
                          :yada.resource/authorized?
                          (fn [ctx]
                            (let [claims (ctx/claims ctx)]
                              (= claims {:user "malcolm"})))

                          :yada.resource/methods
                          {"GET"
                           {:yada.resource/response (fn [ctx]
                                                      (println "Response from protected")
                                                      ;; TODO: Protect with yada.authorization
                                                      "Hi - TODO: show claims in yada.authentication")}}}
                         )
                        :yada.handler/interceptor-chain [authn/authenticate authz/authorize method/perform-method]
                        :yada/profile (profiles :dev)})
          (-> (new-request :get "http://localhost")
              (assoc-in [:headers "cookie"] cookie)))]

    response2))
