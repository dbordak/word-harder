(ns word-harder.handler
  (:require [word-harder.game :as game]
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

(defmethod -event-msg-handler :game/create
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "Creating new game; waiting for second player.")
  (let [game-id (game/create-game uid)]
    (chsk-send! uid
                [:game/create
                 {:what-is-this "New game created, awaiting second player."
                  :how-often "Whenever someone presses 'New Game'."
                  :to-whom uid
                  :from nil
                  :msg game-id}])))

(defmethod -event-msg-handler :game/join
  [{:as ev-msg :keys [event id uid ?data ring-req ?reply-fn send-fn]}]
  (debugf "Initializing game %d, second player %s joined." ?data uid)
  (game/init-game ?data uid)
  (let [game-info (game/get-game ?data)]
    (doseq [player [(:p1 game-info) uid]]
      (chsk-send! player
                  [:game/start
                   {:what-is-this "New game initial data transfer"
                    :how-often "Whenever a game is started and
                      both players have joined."
                    :from nil
                    :to-whom player
                    :msg (game/hide-game-info game-info player)}]))))

;;;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-server-chsk-router!
           ch-chsk event-msg-handler)))
