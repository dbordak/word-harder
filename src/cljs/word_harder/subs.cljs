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
 :word-list
 (fn [db]
   (:word-list db)))

(re-frame/reg-sub
 :user
 (fn [db]
   (:user db)))

(re-frame/reg-sub
 :chat
 (fn [db]
   (:chat db)))

(re-frame/reg-sub
 :chat/msg-input
 (fn [db]
   (-> db :chat :msg-input)))

(re-frame/reg-sub
 :chat/msg-list
 (fn [db]
   (-> db :chat :msg-list)))

(re-frame/reg-sub
 :chat/enabled?
 (fn [db]
   (-> db :chat :enabled?)))

(re-frame/reg-sub
 :chat/ready?
 (fn [db]
   (-> db :chat :ready?)))
