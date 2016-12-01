;; Copyright © 2014-2016, JUXT LTD.

(ns yada.error
  (:require [yada.profile :as profile]
            [yada.context :as ctx]
            [yada.status :as status]
            [clojure.spec :as s]
            [clojure.tools.logging :refer :all]
            [clojure.spec.gen :as gen]
            [clojure.string :as str]))

(defn exception? [e]
  (instance? Exception e))

(s/def :yada.error/exception
  (s/with-gen
    exception?
    #(gen/return (new Exception "Error"))))

(s/def :yada/error
  (s/keys :opt [:yada.error/exception]))

(defmulti render-error ""
  (fn [ctx error]
    (profile/error-renderer ctx)))

(defmethod render-error :default [ctx error]
  "ERROR")

(defn render-minimal-error [ctx]
  (let [status (get-in ctx [:yada/response :yada.response/status])]
    (or
     (str status " " (some-> status status/status :name))
     "Unknown Status")))

(defmethod render-error :minimal [ctx error]
  (render-minimal-error ctx))

(defmethod render-error :full [ctx error]
  (str (render-minimal-error ctx)
       \return \newline
       (when-let [ex (:yada.error/exception error)]
         (str/join (str \return \newline)
                   (for [stl (.getStackTrace ex)]
                     (.toString stl))))))

(defn status [error]
  (let [exception (:yada.error/exception error)
        error-data (when (instance? clojure.lang.ExceptionInfo exception) (ex-data exception))]
    (or (:ring.response/status error-data) 500)))

(defn ^:interceptor handle-error
  "Render the error according to any negotiated content type"
  [ctx]
  (let [error (ctx/error ctx)
        status (status error)
        ctx (assoc-in ctx [:yada/response :yada.response/status]
                      (or status 500))]

    (if-let [ex (:yada.error/exception error)]
      (errorf ex "Error processing request")
      (errorf "Error processing request"))

    (assoc-in
     ctx
     [:yada/response :yada.response/body]
     (render-error ctx error))))
