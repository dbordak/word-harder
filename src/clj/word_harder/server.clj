(ns word-harder.server
  (:require [word-harder.handler :refer [handler start-router!]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn start-server! [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (run-jetty handler {:port port :join? false})))

(defn -main [& args]
  (start-server!)
  (start-router!))
