;; Copyright © 2014-2016, JUXT LTD.

(ns yada.resource-coercion-test
  (:require
   [clojure.test :refer :all]
   [yada.resource :refer [coerce-to-resource-map]]
   [clojure.spec :as s]))

(deftest resource-map-coercion-test
  (is (=
       {:yada.resource/methods [{:yada/method-token "GET"} {:yada/method-token "POST"}]}
       (coerce-to-resource-map {:yada.resource/methods (array-map :get {} :post {})})))

  (is (=
       {:yada.resource/authentication [{:yada.resource/scheme :foo}]}
       (coerce-to-resource-map {:yada.resource/authentication :foo}))))

(deftest generator-test
  (is
   (every? (partial s/valid? :yada/resource)
           (clojure.spec.gen/sample (s/gen :yada/resource)))))

#_(s/explain-str :yada/resource {:yada.resource/authentication-schemes
                               [{:yada.resource/scheme "Basic"
                                 :yada.resource/realm "default"
                                 :yada.resource/authenticate (fn [ctx] ctx)}]
                               :yada.resource/methods [{:yada/method-token "GET"}]})

#_(coerce-to-resource-map {:yada.resource/access-control
                         {:yada.resource/realms []}
                         :yada.resource/methods (array-map :get {} :post {})})
