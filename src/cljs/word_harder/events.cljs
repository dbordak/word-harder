(ns word-harder.events
  (:require [re-frame.core :as re-frame]
            [word-harder.db :as db]
            [taoensso.timbre :as timbre]
            [taoensso.sente :as sente]
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
   db))

(re-frame/reg-event-db
 :game/create-custom
 (fn [db _]
   (chsk-send! [:game/create
                {:tiles (reduce-kv (fn [m k v] (assoc m k (js/parseInt v)))
                                   {} (:tiles (:custom-game-form db)))
                 :wordlists (into (vector) (:wordlists (:custom-game-form db)))}])
   db))

(re-frame/reg-event-db
 :game/join
 (fn [db _]
   (chsk-send! [:game/join (js/parseInt (:game-id-input db))])
   db))

(re-frame/reg-event-db
 :game/claim
 (fn [db _]
   (chsk-send! [:game/claim (:id (:game db))])
   db))

(re-frame/reg-event-db
 :game/hint
 (fn [db _]
   (chsk-send! [:game/hint
                {:id (:id (:game db))
                 :hint (let [hint (:hint-input db)]
                         (str (:word hint)
                              " "
                              (:count hint)))}])
   db))

(re-frame/reg-event-db
 :game/touch
 (fn [db _]
   (chsk-send! [:game/touch
                {:id (:id (:game db))
                 :word (:selected-word db)}])
   (assoc db :selected-word "")))

(re-frame/reg-event-db
 :game/pass
 (fn [db _]
   (chsk-send! [:game/pass (:id (:game db))])
   (assoc db :selected-word "")))

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

(re-frame/reg-event-db
 :set-uid
 (fn [db [_ v]]
   (assoc db :uid v)))

(re-frame/reg-event-db
 :set-player-number
 (fn [db _]
   (assoc db :player-number
          (cond (= (:uid db) (:p1 (:game db))) 1
                (= (:uid db) (:p2 (:game db))) 2))))

(re-frame/reg-event-db
 :tile-input-changed
 (fn [db [_ tile v]]
   (assoc-in db [:custom-game-form :tiles tile] v)))

(re-frame/reg-event-db
 :wordlist-input-changed
 (fn [db [_ v]]
   (assoc-in db [:custom-game-form :wordlists] v)))

(re-frame/reg-event-db
 :game/get-wordlists
 (fn [db _]
   (chsk-send! [:game/get-wordlists] 5000
               (fn [reply]
                 (if (sente/cb-success? reply)
                   (re-frame/dispatch [:set-wordlists reply]))))
   db))

(re-frame/reg-event-db
 :set-wordlists
 (fn [db [_ v]]
   (assoc db :wordlists v)))
