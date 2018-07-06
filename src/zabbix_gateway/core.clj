;;;
;;; For usage see README.md at the root of the repository ...
;;;
(ns zabbix-gateway.core
  "HTTP handler calling Zabbix sender"
  (:gen-class)
  (:require
   [zabbix-gateway.zabbix :as z]
   [clojure.tools.cli :refer [parse-opts]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [ring.util.response :as re]
   [ring.middleware.params :refer [wrap-params]]
   [clojure.pprint :refer [pprint]]    ; FIXME: for debug
   [compojure.core :as cc]
   [compojure.route :as route]))

(defonce config (atom {:zabbix-server "localhost"
                       :zabbix-port 10051}))
;;
;; Send metrics for Zabbix trapper items with "ZBXD\1" TCP protocoll.
;; Beware  that  Numeric(float) item  type  chokes  on large  numbers.
;; Consider using Numeric(unsigned) and convert to long integers here.
;;
;; Zabbix sender format is about that much:
;;
;;     [{:host "test-host"
;;       :key "test.trapper.item.key[metric]"
;;       :value (long 42)}]
;;
(defn- zabbix-sender [metrics]
  (let [cfg @config]
    (z/zabbix-sender (:zabbix-server cfg)
                     (:zabbix-port cfg)
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
(defn- make-reply [request]
  ;; (pprint request)
  (let [metrics (:body request)
        info (zabbix-sender metrics)]
    ;; Just the wrapper "wrap-json-response"  does not suffice --- you
    ;; need to decorate with ring.util.response/response:
    (re/response info)))

(cc/defroutes api-routes
  (cc/POST "/trap" request (make-reply request)))

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

;; See usage in the README.md
(def cli-opts
  [["-p" "--port PORT" "Port number"
    :default 15001
    :parse-fn #(Integer/parseInt %)]
   ["-H" "--zabbix-server HOSTNAME" "Zabbix server/proxy"
    :default "localhost"]
   ["-P" "--zabbix-port PORT" "Zabbix server/proxy port"
    :default 10051
    :parse-fn #(Integer/parseInt %)]])

(defn -main
  "Starts a webserver at specified port number (default is 15001)"
  [& args]
  (let [opts (parse-opts args cli-opts)]
    #_(println opts)
    ;; We do not expect any positional arguments:
    (when (> (count (:arguments opts)) 0)
      (println (str "Usage:\n"
                    (:summary opts)))
      (System/exit 1))
    ;; Store config in a global atom:
    (swap! config (fn [_] (:options opts))))
  (println "Starting server. Config:" @config)
  (let [port (:port @config)]
    (make-and-start-server port)))
