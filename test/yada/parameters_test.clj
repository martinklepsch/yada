(ns yada.parameters-test
  (:require
   [byte-streams :as bs]
   [clojure.edn :as edn]
   [clojure.test :refer :all]
   [yada.yada :refer (yada)]
   [yada.test.util :refer (given)]
   [ring.mock.request :as mock]
   [ring.util.codec :as codec]
   [schema.core :as s]))


(deftest post-test
  (let [handler (yada nil
                      :parameters {:post {:form {:foo s/Str}}}
                      :post! (fn [ctx] (pr-str (:parameters ctx))))]

    ;; Nil post body
    (let [response (handler (mock/request :post "/"))]
      (given @response
        :status := 200
        [:body bs/to-string edn/read-string] := nil))

    ;; Form post body
    (let [response (handler (mock/request :post "/"
                                          {"foo" "bar"}))]
      (given @response
        :status := 200
        [:body bs/to-string edn/read-string] := {:foo "bar"}))))

(deftest post-test-with-query-params
  (let [handler (yada nil
                      :parameters {:post {:query {:foo s/Str}
                                          :form {:bar s/Str}}}
                      :post! (fn [ctx] (pr-str (:parameters ctx))))]

    ;; Nil post body
    #_(let [response (handler (mock/request :post "/?foo=123"))]
      (given @response
        :status := 200
        [:body bs/to-string edn/read-string] := {:foo "123"}))

    ;; Form post body
    (let [response (handler (mock/request :post "/?foo=123"
                                          {"bar" "456"}))]
      (given @response
        :status := 200
        [:body bs/to-string edn/read-string] := {:foo "123" :bar "456"}))))