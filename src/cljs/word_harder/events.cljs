(ns word-harder.events
  (:require [re-frame.core :as re-frame]
            [word-harder.db :as db]
            [taoensso.timbre :as timbre]
            [word-harder.ws :refer [chsk-send!]]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [db _]
   (merge db db/default-db)))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 :game-id-input-changed
 (fn [db [_ v]]
   (assoc db :game-id-input v)))

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
 :game/create
 (fn [db _]
   (chsk-send! [:game/create])
   (assoc db :player-number 1)))

(re-frame/reg-event-db
 :game/join
 (fn [db _]
   (chsk-send! [:game/join (js/parseInt (:game-id-input db))])
   (assoc db :player-number 2)))

(re-frame/reg-event-db
 :game/claim
 (fn [db _]
   (chsk-send! [:game/claim (:id (:game db))])
   db))

(re-frame/reg-event-db
 :set-board
 (fn [db [_ v]]
   (assoc-in db [:game :board] v)))

(re-frame/reg-event-db
 :set-game-id
 (fn [db [_ v]]
   (assoc-in db [:game :id] v)))

(re-frame/reg-event-db
 :set-game-info
 (fn [db [_ v]]
   (assoc db :game v)))
