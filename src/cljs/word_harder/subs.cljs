(ns word-harder.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 :hint-input
 (fn [db]
   (:hint-input db)))

(re-frame/reg-sub
 :count
 (fn [db]
   (:count db)))

(re-frame/reg-sub
 :hint
 (fn [db]
   (:hint db)))

(re-frame/reg-sub
 :selected-word
 (fn [db]
   (:selected-word db)))

(re-frame/reg-sub
 :board
 (fn [db]
   (:board db)))
