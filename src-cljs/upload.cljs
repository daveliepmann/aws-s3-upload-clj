(ns awsuplclj
  (:require [ajax.core :as ajax :refer [POST]]
            [cljs.core.async :as async :refer [chan put!]]
            [cljs-time.core :as t]
            [cljs-time.format :as tf])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def upload-queue (atom []))

(defn listen
  "Listen for events of `type` on element `el`.
   Return asynchronous channel with event. Cribbed with love from
   swannodette's cljs 101 core.async tutorial:
   http://swannodette.github.io/2013/11/07/clojurescript-101/"
  [el type]
  (let [out (chan)]
    (goog.events/listen el type
                   (fn [e] (put! out e)))
    out))

(defn enqueue-file!
  "Add `file` to the upload queue."
  [file]
  (swap! upload-queue conj {:file file
                            :formdata (doto (js/FormData. (.getElementById js/document "s3_fields"))
                                        (.append "Content-Type" (.-type file))
                                        (.append "file" file (str (.-name file))))}))

(defn upload-to-s3!
  "Upload file-containing `form-data` to Amazon Web Services' Simple
  Storage Service (AWS S3) using credentials in
  `s3-template-form`. Report completion on asynchronous channel `ch`."
  [form-data s3-template-form ch]
  (let [req       (js/XMLHttpRequest.)]
    ;; Status 4 means 'DONE', per https://developer.mozilla.org/en/docs/Web/API/XMLHttpRequest
    (aset req "onreadystatechange" #(when (= (.-readyState req) 4)
                                      (go (>! ch req))))
    (.open req "POST" (aget s3-template-form "action"))
    (.send req form-data)))

(defn listen-for-files!
  "Initialize a listener to detect the selection of files and add them
  to the `upload-queue`."
  []
  (go (while true
        (doseq [file (array-seq (.-files (.-target (<! (listen (.getElementById js/document "files")
                                                               "change")))))]
          (enqueue-file! file)))))

(defn upload-files []
  (go (let [c (chan 1)]
        (doseq [f @upload-queue]
          (upload-to-s3! (:formdata f) (.getElementById js/document "s3_fields") c)
          (when (= 201 (.-status (<! c))) ;; Successfully uploaded this file
            )))))

(aset js/document "onreadystatechange" #(when (= "complete" (. js/document -readyState))
                                          (listen-for-files!)))
