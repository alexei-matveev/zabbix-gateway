(ns server-clj.core
  "This namespace will be renamed!"
  (:gen-class)
  (:require
    [ring.adapter.jetty :refer [run-jetty]]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [ring.middleware.params :refer [wrap-params]]
    [clojure.pprint :refer [pprint]]    ; FIXME: for debug
    [clj-time.core :as time]            ; FIXME: not in project.clj
    [compojure.core :as cc]
    [compojure.route :as route]))

;;
;; We could  be parsing  JSON ourselves  here.  The  body of  the POST
;; request is  by default  some HttpInputOverHTTP object  (instance of
;; InputStream by  ring spec). One could  just slurp it into  a string
;; and parse.  However the wrap-json-body handler decorator deals with
;; it for us. It will not  do anything if the Content-Type Header does
;; not indicate a JSON content though.  Parse-csv seems to accept UTF8
;; with BOM but the JSON parser is not so cooperative:
;;
;; http_proxy="" curl -XPOST -H "Content-Type: application/json"
;; --data-binary @test-no-bom.json http://localhost:5001/post-json
;;
(defn- make-post-reply [request]
  ;; (pprint request)

  ;; The parser  output isa LazySeq,  conversion to str does  not show
  ;; its  content,  but printing  does  use  (vec ...)  instead.  Data
  ;; arrival time stamp is common for all entries:
  (let [body (:body request)]
    (str "ok\n")))

(cc/defroutes api-routes
  (cc/POST "/post-json" request (time (make-post-reply request))))

;;
;; This magic is to make the handler for Jetty adapter. There are also
;; other defaults in  the ring middleware. FIXME:  The default include
;; anti-forgery  (CSRF) protection  that makes  curl POST-ing  without
;; fetching   a  token   first  impossible   (?).   See   for  example
;; http://www.luminusweb.net/docs/security.md
;;
(def site
  (cc/routes
   ;; Parse request body as JSON and keywordize JSON keys. Wrap-params
   ;; added to handle the query string:
   (-> api-routes
       wrap-params
       (wrap-json-body {:keywords? true})
       wrap-json-response)))

;;
;; For interactive  use (def server (make-and-start-server  3000)) and
;; then (.stop server) (.start  server). Eventually re-load and re-def
;; the server when the handler changes.
;;
(defn make-and-start-server [port]
  (run-jetty site {:port port :join? false}))

;;
;; To check if the server runs:
;;
;;     http_proxy="" curl -XPOST http://localhost:15001/post-json -d 1
;;
;; or
;;
;;     echo 1 | http_proxy= "" curl -XPOST http://localhost:15001/post-json -d @-
;;
(defn -main
  "Starts a webserver at specified port number (default is 15001)"
  [& [port]]
  (let [port (Integer. (or port 15001))]
    (make-and-start-server port)        ; non-blocking
    (prn "server is running...")))
