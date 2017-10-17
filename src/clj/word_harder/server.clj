(ns word-harder.server
  (:require [word-harder.handler :refer [handler start-router!]]
            [config.core :refer [env]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defn start-server! [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (run-server handler {:port port :join? false})))

(defn -main [& args]
  (start-server!)
  (start-router!))
