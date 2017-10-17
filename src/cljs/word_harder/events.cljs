(ns word-harder.events
  (:require [re-frame.core :as re-frame]
            [word-harder.db :as db]
            [taoensso.timbre :as timbre]
            [word-harder.ws :refer [chsk-send!]]
            [secretary.core :as secretary]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [db _]
   (merge db db/default-db)))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 :test/send
 (fn [db [_ msg]]
   (chsk-send! [:test/send msg])
   db))

(re-frame/reg-event-db
 :hint-input-changed
 (fn [db [_ v]]
   (assoc db :hint-input
          (merge (:hint-input db) v))))

(re-frame/reg-event-db
 :selected-word
 (fn [db [_ v]]
   (assoc db :selected-word v)))

(re-frame/reg-event-db
 :game/new
 (fn [db _]
   (chsk-send! [:game/new] 5000
               (fn [cb-reply]
                 (when cb-reply
                   (secretary/dispatch! "#/game"))))
   db))

(re-frame/reg-event-db
 :board
 (fn [db [_ v]]
   (assoc db :board v)))
