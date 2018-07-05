;; See the sample project file for other options:
;; https://github.com/technomancy/leiningen/blob/master/sample.project.clj
(defproject server-clj "0.1.0-SNAPSHOT"
  :description "HTTP to Zabbix Sender/Trapper"
  :url "https://github.com/alexei-matveev/zabbix-gateway"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.7"]
                 [ring/ring-core "1.7.0-RC1"]
                 [ring/ring-jetty-adapter "1.7.0-RC1"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.6.1"]]
  ;; Profiles.  Each  active  profile  gets merged  into  the  project
  ;; map. The :dev  and :user profiles are active by  default, but the
  ;; latter should  be looked  up in ~/.lein/profiles.clj  rather than
  ;; set in  project.clj.  Use the with-profiles  higher-order task to
  ;; run a  task with a different  set of active profiles.   See `lein
  ;; help profiles` for a detailed explanation.
  :profiles {:uberjar {:aot :all}}      ; activated for uberjar
  ;; You can set JVM-level options here. The :java-opts key is an
  ;; alias for this.
  :jvm-opts ["-Xmx128m" "-Xms64m"]
  ;; Only for uberjar (see :profiles) othewise "lein run" is slower:
  ;; :aot [server-clj.core]
  :main zabbix-gateway.core)
