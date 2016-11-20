;; Copyright © 2014-2016, JUXT LTD.

(ns yada.resource
  (:require
   [clojure.walk :refer [postwalk]]
   [clojure.string :as str]
   [clojure.spec :as s]
   yada.spec))

;; Specs

(s/def :yada.resource/method
  (s/keys :req [:yada/method-token]
          :opt [:yada.resource/response]))

(s/def :yada.resource/methods
  (s/+ :yada.resource/method))

(s/def :yada/resource
  (s/keys :req [:yada.resource/methods]))

;; Coercion

(defn matches-map-entry? [k candidate]
  (and (vector? candidate) (= (count candidate) 2) (= (first candidate) k)))

(defn coerce-method [x] x)

(defn coerce-methods [x]
  (cond
    (map? x) (mapv (fn [[k v]] (coerce-method (merge {:yada/method-token (str/upper-case (name k))} v))) x)
    (vector? x) (mapv coerce-method x)
    :otherwise x))

(defn coerce-to-resource-map [input]
  (postwalk
   (fn [x]
     (cond
       (matches-map-entry? :yada.resource/methods x) [:yada.resource/methods (coerce-methods (second x))]
       :otherwise x))
   input))

;; Protocols and Records

(defprotocol ResourceCoercion
  (as-resource [_] "Coerce an existing type to a
  resource. Implementations SHOULD return a resource map but may
  return a resource if the implementation wants to ensure conformity
  of the resource-map"))

(defrecord Resource []
  ;; A resource can be coerced to itself
  ResourceCoercion
  (as-resource [this] this))

(defn ^{:doc "Create a resource record. This will assume model is
  pre-validated (and check for this, throwing an exception if it is
  not)."
        :commentary "This function exists to avoid the coercion step
  for resource models that have already been coerced. Such a step
  would be harmless, so this can be considered a performance
  optimisation."}

  resource* [model]

  (when-not (s/valid? :yada/resource model)
    (throw
     (ex-info
      (format "Resource model is not valid: %s" (s/explain-str :yada/resource model))
      {:model model :explain (s/explain-data :yada/resource model)})))
  ;; TODO: Must also pre-compute anything that is pre-computable, everything else will be per-request
  (map->Resource model))

(defn resource
  "Create a resource record. An attempt is made to coerce the given
  model so that it passes validation."
  [model]
  (resource* (coerce-to-resource-map model)))

(defn lookup-method [resource token]
  (first (filter (fn [m] (= (:yada/method-token m) token)) (-> resource :yada.resource/methods))))
