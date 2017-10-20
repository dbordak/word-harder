(ns word-harder.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com]))


;; home

(defn home-title []
  [re-com/title
   :label "Words 2: Word Harder"
   :level :level1
   :style {:margin-bottom "1em"}])

(defn new-game-button []
  [re-com/button
   :label "Start a New Game"
   :on-click #(re-frame/dispatch [:game/create])])

(defn join-game-box []
  (let [game-id-input (re-frame/subscribe [:game-id-input])]
    (fn []
      [:form {:id "game-id-form"}
       [re-com/input-text
        :placeholder "Game ID"
        :width "6em"
        :model @game-id-input
        :on-change #(re-frame/dispatch [:game-id-input-changed %])]
       [re-com/button
        :label "Join a Game"
        :on-click #(do (re-frame/dispatch [:game/join])
                       (.preventDefault %))]])))

(defn home-panel []
  [re-com/v-box
   :gap "1em"
   :align :center
   :children [[home-title]
              [new-game-button]
              [re-com/title
               :label "- or -"
               :level :level4
               :style {:margin "0"}]
              [join-game-box]]])


;; Lobby

(defn lobby-panel []
  (let [game-id (re-frame/subscribe [:game-id])]
    (fn []
      [re-com/v-box
       :gap "1em"
       :children [[re-com/title
                   :label "Game created. Awaiting Player 2."
                   :level :level1]
                  [re-com/title
                   :label (str "Game ID: " @game-id)
                   :level :level4]]])))


;; Game

(defn settings-modal []
  [re-com/md-icon-button
   :md-icon-name "zmdi-settings"])

(defn hint-display []
  (let [hint (re-frame/subscribe [:hint])]
    (fn []
      [re-com/label
       :label (if @hint
                @hint
                "No hint to display.")
       :class "btn btn-primary disabled"])))

(defn help-modal []
  [re-com/md-icon-button
   :md-icon-name "zmdi-help"])

(defn top-bar []
  [re-com/h-box
   :gap "0.5em"
   :children [[settings-modal] [hint-display] [help-modal]]])

(defn word-button [space]
  (let [selected-word (re-frame/subscribe [:selected-word])
        player-number (re-frame/subscribe [:player-number])
        hint (re-frame/subscribe [:hint])
        other-player-number (- 3 player-number)
        word (:word space)
        turn (re-frame/subscribe [:turn])
        your-turn? (or (= player-number @turn)
                       (nil? @turn))]
    [re-com/button
     :label word
     :disabled? (or
                 ;; your turn = you're giving hint
                 your-turn?
                 ;; collapsed = already resolved
                 (not (:superposition space))
                 ;; you already touched it
                 (some #{@player-number} (:touched-by space))
                 ;; hint not ready yet
                 (nil? @hint))
     :class (str "button space "
                       (if (= word @selected-word)
                         "active" "")
                       " "
                       (cond
                         (not (:superposition space)) (:colors space)
                         your-turn? (nth (:colors space) (- @player-number 1))
                         :default "u")
                       " "
                       (if (:superposition space)
                         (str
                          (if (some #{@player-number} (:touched-by space))
                            "t-you" "")
                          " "
                          (if (some #{other-player-number} (:touched-by space))
                            "t-them" ""))
                         ""))
     :on-click #(re-frame/dispatch [:selected-word word])]))

(defn game-board []
  (let [board (re-frame/subscribe [:board])]
    (fn []
      [re-com/h-box
       :children (doall (map word-button @board))
       :max-width "50em"
       :justify :between
       :style {:flex-flow "row wrap"}])))

(defn hint-input []
  (let [new-hint (re-frame/subscribe [:hint-input])]
    (fn []
      [re-com/h-box
       :gap "0.5em"
       :children [[re-com/input-text
                   :placeholder "Hint"
                   :model (:word @new-hint)
                   :on-change #(re-frame/dispatch [:hint-input-changed {:word %}])]
                  [re-com/input-text
                   :placeholder "Count"
                   :model (:count @new-hint)
                   :width "5em" ; number input, only need it to be as
                                ; big as the placeholder text.
                   :on-change #(re-frame/dispatch [:hint-input-changed {:count %}])]
                  [re-com/button
                   :label "Submit"
                   :on-click #(do (re-frame/dispatch [:hint-input-changed {:word "" :count ""}]))]]])))

(defn action-row []
  (let [turn (re-frame/subscribe [:turn])
        player-number (re-frame/subscribe [:player-number])]
    (cond
      (nil? @turn) [re-com/button
                    :label "Claim First Turn"
                    :on-click #(re-frame/dispatch [:game/claim])]
      (= @turn @player-number) [hint-input]
      :default [re-com/button
                :label "Touch"
                :on-click #(re-frame/dispatch [:game/touch])])))

(defn game-panel []
  [re-com/v-box
   :gap "1em"
   :align :center
   :children [[top-bar] [game-board] [action-row]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :lobby-panel [lobby-panel]
    :game-panel [game-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [re-com/v-box
       :height "100%"
       :justify :center
       :align :center
       :children [[panels @active-panel]]])))
