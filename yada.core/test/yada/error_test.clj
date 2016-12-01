;; Copyright © 2014-2016, JUXT LTD.

(ns yada.error-test
  (:require [clojure.test :refer :all]
            [yada.handler :refer [new-handler accept-request]]
            [yada.method :as method]
            [yada.profiles :refer [profiles]]
            [yada.resource :refer [new-resource]]
            [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.test.check :as tc]
            [yada.error :as error]
            [yada.context :refer [new-context]]
            [clojure.test.check.properties :as prop]
            [yada.test-util :refer [new-request]]))

#_(def property
  (prop/for-all [error (s/gen :yada.response/error)]
                (error/handle-error (new-context {:yada/error (new Exception "Whoops")}))
                false))


#_(tc/quick-check 10 property)
;; test our property
#_(deftest qc
  (tc/quick-check 100 property))

#_(gen/generate )

(defn force-error [profile]
  (let [handler
        (new-handler
         {:yada/resource
          (new-resource
           {:yada.resource/methods
            {"GET"
             {:yada.resource/response
              (fn [ctx] (throw (new Exception "Whoops")))}}})
          :yada.handler/interceptor-chain [method/perform-method]
          :yada/profile (get profiles profile)})
        ]
    @(accept-request handler (new-request :get "http://localhost"))))

(deftest error-test
  (is (> 100 (count (:body (force-error :prod)))))
  (is (< 100 (count (:body (force-error :dev))))))
