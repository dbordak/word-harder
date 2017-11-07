(ns word-harder.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 :game-id-input
 (fn [db _]
   (:game-id-input db)))

(re-frame/reg-sub
 :game-id
 (fn [db _]
   (:id (:game db))))

(re-frame/reg-sub
 :hint-input
 (fn [db]
   (:hint-input db)))

(re-frame/reg-sub
 :hint
 (fn [db]
   (:hint (:game db))))

(re-frame/reg-sub
 :selected-word
 (fn [db]
   (:selected-word db)))

(re-frame/reg-sub
 :board
 (fn [db]
   (:board (:game db))))

(re-frame/reg-sub
 :player-number
 (fn [db]
   (:player-number db)))

(re-frame/reg-sub
 :turn
 (fn [db]
   (:turn (:game db))))

(re-frame/reg-sub
 :hints
 (fn [db]
   (:hints (:game db))))

(re-frame/reg-sub
 :mistakes
 (fn [db]
   (:mistakes (:game db))))

(re-frame/reg-sub
 :won
 (fn [db]
   (:won (:game db))))

(re-frame/reg-sub
 :custom-game-form
 (fn [db]
   (:custom-game-form db)))

(re-frame/reg-sub
 :wordlists
 (fn [db]
   (:wordlists db)))
