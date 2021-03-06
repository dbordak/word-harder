(ns word-harder.handler
  (:require [word-harder.game :as game]
            [word-harder.db :as db]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response content-type]]
            [clojure.core.async :as async :refer (<! <!! >! >!! put! chan go go-loop)]
            [taoensso.timbre :as timbre :refer (tracef debugf infof warnf errorf)]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket! (get-sch-adapter)
                                  {:user-id-fn (fn [ring-req]
                                                 (:client-id ring-req))})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(defroutes routes
  (GET "/" [] (content-type
               (resource-response "index.html" {:root "public"})
               "text/html"))
  (GET "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req))
  (resources "/"))

(def handler (wrap-defaults #'routes site-defaults))
(def dev-handler (wrap-reload handler))

(add-watch connected-uids :connected-uids
  (fn [_ _ old new]
    (when (not= old new)
      (infof "Connected uids change: %s" new))))

(defn send-updated-game-state [game-id from]
  (let [game-info (db/get-game game-id)]
    (doseq [player [(:p1 game-info) (:p2 game-info)]]
      (when (not (nil? player))
        (chsk-send! player
                    [:game/update
                     {:what-is-this "Game info object update"
                      :how-often "Whenever game state is changed."
                      :from from
                      :to-whom player
                      :msg (game/hide-game-info game-info player)}])))))

;; Receiving

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (future (-event-msg-handler ev-msg)))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (debugf "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defmethod -event-msg-handler :chsk/ws-ping
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  nil)

(defmethod -event-msg-handler :chsk/uidport-close
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "%s disconnected; removing from active games" uid)
  (doseq [game-info (db/get-games-by-player uid)]
    (debugf "Removing %s from game %s" uid (:id game-info))
    (db/set-player (:id game-info) (db/player-number uid) nil)
    (send-updated-game-state (:id game-info) nil)))

(defmethod -event-msg-handler :game/get-wordlists
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "Client requested wordlists.")
  (?reply-fn (db/list-word-categories)))

(defmethod -event-msg-handler :game/create
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "Creating new game; waiting for second player.")
  (if ?data (debugf "New game config: %s" ?data))
  (let [game-id
        (if ?data
          (db/create-game
           uid
           (game/create-board :wordlists (:wordlists ?data)
                              :custom-wordlist (:custom-wordlist ?data)
                              :key (game/create-key (:tiles ?data)))
           :hints (:hints ?data)
           :mistakes (:mistakes ?data))
          (db/create-game uid (game/create-board)))]
    (chsk-send! uid
                [:game/waiting
                 {:what-is-this "New game created, awaiting second player."
                  :how-often "Whenever someone presses 'New Game'."
                  :to-whom uid
                  :from nil
                  :msg game-id}])))

(defmethod -event-msg-handler :game/join
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "Player %s attempting to join game %s" uid ?data)
  (let [game-info (db/get-game ?data)
        player-1 (:p1 game-info)
        player-2 (:p2 game-info)]
    (if (and (nil? player-1) (nil? player-2))
      (do (db/set-player (:id game-info) 1 uid)
          (chsk-send! uid
                      [:game/waiting
                       {:what-is-this "Frozen game rejoined, awaiting second player."
                        :how-often "Whenever someone rejoins a game with no players."
                        :to-whom uid
                        :from nil
                        :msg (:id game-info)}]))
      ;; if there's already someone in this game:
      (do (cond
            (nil? player-1) (db/set-player (:id game-info) 1 uid)
            (nil? player-2) (db/set-player (:id game-info) 2 uid))
          (let [new-game-info (db/get-game ?data)]
            (doseq [player [(:p1 new-game-info) (:p2 new-game-info)]]
              (chsk-send! player
                          [:game/start
                           {:what-is-this "Initial data transfer."
                            :how-often "Whenever a game is started and
                            both players have joined, or a second player
                            rejoins an in-progress game."
                            :from nil
                            :to-whom player
                            :msg (game/hide-game-info new-game-info player)}])))))))

(defmethod -event-msg-handler :game/claim
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "%s tried to claim first player in game %s" uid ?data)
  (let [game-info (db/get-game ?data)]
    (when (nil? (:turn game-info))
      (game/set-turn game-info uid)
      (send-updated-game-state ?data uid))))

(defmethod -event-msg-handler :game/hint
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "%s tried to set a hint." uid)
  (let [game-info (db/get-game (:id ?data))]
    (when (= (:turn game-info) (db/player-number uid))
      (db/set-hint (:id ?data) (:hint ?data))
      (send-updated-game-state (:id ?data) uid))))

(defmethod -event-msg-handler :game/touch
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "%s tried to touch %s" uid (:word ?data))
  (let [game-info (db/get-game (:id ?data))]
    (when (= (- 3 (:turn game-info)) (db/player-number uid))
      (db/update-board (:id ?data)
                       (:board (game/touch-space-in-game! game-info (:word ?data))))
      (let [post-data (db/get-game (:id ?data))]
        (when (and (<= (:hints post-data) 0)
                   (not (:won post-data)))
          (db/game-over (:id ?data) false)))
      (send-updated-game-state (:id ?data) uid))))

(defmethod -event-msg-handler :game/pass
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "%s tried to pass" uid)
  (let [game-info (db/get-game ?data)]
    (when (= (- 3 (:turn game-info)) (db/player-number uid))
      (game/next-turn game-info)
      (when (<= (:hints (db/get-game ?data)) 0)
        (db/game-over ?data false))
      (send-updated-game-state ?data uid))))

;;;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-server-chsk-router!
           ch-chsk event-msg-handler)))
