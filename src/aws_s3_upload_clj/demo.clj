(ns aws-s3-upload-clj.demo
  (:use [hiccup page]
        [ring.util.codec])
  (:import (javax.crypto Mac)
           (javax.crypto.spec SecretKeySpec))
  (:require [compojure.core :refer [GET defroutes]]
            [clj-time.core :as time]
            [clj-time.format :as time-f]
            [clojure.data.json :as json]
            [compojure.route :as route]
            [noir.util.middleware :refer [app-handler]]))

(def s3-bucket "insert-your-bucket-name-here") ;; FIXME

;; you'll need to set these on your own machine, obvs:
(def aws-access-key (System/getenv "AWS_ACCESS_KEY")) ;; FIXME
(def aws-secret-key (System/getenv "AWS_SECRET_KEY")) ;; FIXME

(defn expiration-date
  "Given today's date as `yyyymmdd` and integer `d`, returns an expiry
  date based on today plus that many days, formatted as date-time."
  [yyyymmdd d]
  (time-f/unparse (time-f/formatters :date-time)
                  (time/plus (time-f/parse
                              (time-f/formatters :basic-date)
                              yyyymmdd) (time/days d))))

(defn policy
  "Given today's date as `yyyymmdd`, returns a base-64-encoded policy
  document for AWS POST uploads to S3."
  [yyyymmdd]
  (ring.util.codec/base64-encode
   (.getBytes (json/write-str { "expiration" (expiration-date yyyymmdd 1),
                                "conditions" [{"bucket" s3-bucket}
                                              {"acl" "public-read"}
                                              ["starts-with", "$Content-Type", ""],
                                              ["starts-with", "$key" ""],
                                              {"success_action_status" "201"}]})
              "UTF-8")))

(defn hmac-sha1 [key string]
  "Returns signature of `string` with a given `key` using SHA-1 HMAC."
  (ring.util.codec/base64-encode
   (.doFinal (doto (javax.crypto.Mac/getInstance "HmacSHA1")
               (.init (javax.crypto.spec.SecretKeySpec. (.getBytes key) "HmacSHA1")))
             (.getBytes string "UTF-8"))))

(defn upload-page
  "Display form to allow user to upload multiple files to AWS S3."
  [& [params]]
  (html5   
   [:meta {:charset "UTF-8"}]
   [:title "Batch uploads to AWS S3 using Clojure"]
   
   [:body.upload
    (let [policy (policy (time-f/unparse (time-f/formatters :basic-date) (time/now)))]
      [:main       
       [:form#s3_upload
        [:input#files {:type "file" :name "file" :multiple "multiple"}]]
       
       [:button#upload_trigger
        {:onclick "javascript:awsuplclj.upload_files();"}
        "Upload files to AWS S3"]
       
       [:form#s3_fields.hidden
        {:action (str "http://" s3-bucket ".s3.amazonaws.com/")
         :method "POST" :enctype "multipart/form-data"}          
        [:input {:type "hidden" :name "key"
                 :value "${filename}"}]
        [:input {:type "hidden" :name "success_action_status"
                 :value "201"}]
        [:input {:type "hidden" :name "acl"
                 :value "public-read"}]
        [:input {:type "hidden" :name "policy"
                 :value policy}]
        [:input {:type "hidden" :name "AWSAccessKeyId"
                 :value aws-access-key}]
        [:input {:type "hidden" :name "signature"
                 :value (hmac-sha1 aws-secret-key policy)}]]])
    (include-js "/js/cljs.js")]))

(defroutes aws-routes
  (GET "/" [] (upload-page))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (app-handler [aws-routes]))
