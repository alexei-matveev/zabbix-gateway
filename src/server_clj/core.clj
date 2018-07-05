(ns server-clj.core
  "This namespace will be renamed!"
  (:gen-class)
  (:require
    [server-clj.zabbix :as z]
    [ring.adapter.jetty :refer [run-jetty]]
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [ring.util.response :as re]
    [ring.middleware.params :refer [wrap-params]]
    [clojure.pprint :refer [pprint]]    ; FIXME: for debug
    [compojure.core :as cc]
    [compojure.route :as route]))

;; Send   the   result  to   Zabbix   trapper   items.   Beware   that
;; Numeric(float) item  type chokes on large  numbers.  Consider using
;; Numeric(unsigned) and convert to long integers here.
(defn- zbx-send []
  (let [metrics [{:host "test-host"
                  :key "test.trapper.item.key[metric]"
                  :value (long 42)}]]
    (z/zabbix-sender "localhost"
                     10051
                     metrics)))

;;
;; We could  be parsing  JSON ourselves  here.  The  body of  the POST
;; request is  by default  some HttpInputOverHTTP object  (instance of
;; InputStream by  ring spec). One could  just slurp it into  a string
;; and parse.  However the wrap-json-body handler decorator deals with
;; it for us. It will not  do anything if the Content-Type Header does
;; not indicate a JSON content though.  Parse-csv seems to accept UTF8
;; with BOM but the JSON parser is not so cooperative:
;;
;; http_proxy="" curl -XPOST -H "Content-Type: application/json" http://localhost:15001/post-json -d @file.json
;;
(defn- make-reply [request]
  (pprint request)

  ;; The parser  output isa LazySeq,  conversion to str does  not show
  ;; its  content,  but printing  does  use  (vec ...)  instead.  Data
  ;; arrival time stamp is common for all entries:
  (let [body (:body request)]
    (println body)
    ;; Just the wrapper "wrap-json-response" does not suffice you need
    ;; to decorate with ring.util.response/response:
    (re/response {:pong body})))

(cc/defroutes api-routes
  (cc/POST "/post-json" request (make-reply request)))

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
;; See "curl" usage above ...
;;
(defn -main
  "Starts a webserver at specified port number (default is 15001)"
  [& [port]]
  (let [port (Integer. (or port 15001))
        ;; This is non blocking:
        server (make-and-start-server port)]
    (println "server is running..." server)
    server))
