(ns word-harder.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [word-harder.events]
            [word-harder.subs]
            [word-harder.routes :as routes]
            [word-harder.views :as views]
            [word-harder.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))
