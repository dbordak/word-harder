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
 :test/send
 (fn [db [_ msg]]
   (chsk-send! [:test/send msg])
   db))

(re-frame/reg-event-db
 :user
 (fn [db [_ v]]
   (assoc db :user
          (merge (:user db) v))))

(re-frame/reg-event-db
 :user/name
 (fn [db [_ v]]
   (assoc-in db [:user :name] v)))

(re-frame/reg-event-db
 :user/email
 (fn [db [_ v]]
   (assoc-in db [:user :email] v)))

(re-frame/reg-event-db
 :hint-input-changed
 (fn [db [_ v]]
   (assoc db :hint-input
          (merge (:hint-input db) v))))

(re-frame/reg-event-db
 :chat/msg-input
 (fn [db [_ v]]
   (assoc-in db [:chat :msg-input] v)))

(re-frame/reg-event-db
 :selected-word
 (fn [db [_ v]]
   (assoc db :selected-word v)))

(re-frame/reg-event-db
 :chat/recv-msg
 (fn [db [_ from msg]]
   (assoc-in db [:chat :msg-list]
          (conj (-> db :chat :msg-list)
                {:name from :body msg}))))

(re-frame/reg-event-db
 :chat/enabled?
 (fn [db [_ v]]
   (assoc-in db [:chat :enabled?] v)))

(re-frame/reg-event-db
 :chat/ready?
 (fn [db [_ v]]
   (assoc-in db [:chat :ready?] v)))

(re-frame/reg-event-db
 :chat/send-msg
 (fn [db _]
   (chsk-send! [:chat/msg (-> db :chat :msg-input)])
   (assoc-in db [:chat :msg-input] "")))

(re-frame/reg-event-db
 :chat/send-user-info
 (fn [db _]
   (chsk-send! [:chat/user (:user db)] 5000
               (fn [cb-reply]
                 (when cb-reply
                   (re-frame/dispatch [:chat/ready? true]))))
   ;; not going to clear the forms since we might want these values,
   ;; and these <input>s disappear as soon as they're set
   db))
