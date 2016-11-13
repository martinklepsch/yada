;; Copyright © 2015, JUXT LTD.

(ns yada.media-type
  (:refer-clojure :exclude [type])
  (:require [clojure.tools.logging :refer :all]
            [yada.util :refer [http-token OWS]]))

;; For implementation efficiency, we keep the parsed versions of media
;; types as records rather than encode in Strings which require
;; reparsing. This is the sole purpose of the MediaTypeMap record below.

(defrecord MediaTypeMap [name type subtype parameters quality])

(def media-type-pattern
  (re-pattern (str "(" http-token ")"
                   "/"
                   "(" http-token ")"
                   "((?:" OWS ";" OWS http-token "=" http-token ")*)")))

(def media-type-pattern-no-subtype
  (re-pattern (str "(\\*)"
                   "((?:" OWS ";" OWS http-token "=" http-token ")*)")))

;; TODO: Replace memoize with cache to avoid memory exhaustion attacks
(def string->media-type
  (memoize
   (fn [s]
     (when s
       (let [g (rest (or (re-matches media-type-pattern s)
                         (concat (take 2 (re-matches media-type-pattern-no-subtype s))
                                 ["*" (last (re-matches media-type-pattern-no-subtype s))])))
             params (into {} (map vec (map rest (re-seq (re-pattern (str ";" OWS "(" http-token ")=(" http-token ")"))
                                                        (last g)))))]
         (->MediaTypeMap
          (str (first g) "/" (second g))
          (first g)
          (second g)
          (dissoc params "q")
          (if-let [q (get params "q")]
            (try
              (Float/parseFloat q)
              (catch java.lang.NumberFormatException e
                (float 1.0)))
            (float 1.0))))))))

;; TODO: Replace memoize with cache to avoid memory exhaustion attacks
(def media-type->string
  (memoize
   (fn [mt]
     (when mt
       (assert (instance? MediaTypeMap mt))
       (.toLowerCase
        (str (:name mt)
             (apply str (for [[k v] (:parameters mt)]
                          (str ";" k "=" v)))))))))