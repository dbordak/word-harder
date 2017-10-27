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

(defn info-bar []
  (fn []
    (let [turns (re-frame/subscribe [:hints])
          mistakes (re-frame/subscribe [:fails])]
      [re-com/h-box
       :justify :between
       :width "100%"
       :children [[re-com/title
                   :label (str "Turns Remaining: " @turns)
                   :level :level3]
                  [re-com/title
                   :label (str "Mistakes Remaining: " @mistakes)
                   :level :level3]]])))

(defn settings-modal []
  [re-com/md-icon-button
   :style {:display "none"}
   :md-icon-name "zmdi-settings"])

(defn hint-display []
  (let [hint (re-frame/subscribe [:hint])]
    (fn []
      [re-com/title
       :label (if @hint
                @hint
                "No hint to display.")
       :level :level2])))

(defn help-modal []
  [re-com/md-icon-button
   :style {:display "none"}
   :md-icon-name "zmdi-help"])

(defn hint-bar []
  [re-com/h-box
   :gap "0.5em"
   :children [[settings-modal] [hint-display] [help-modal]]])

(defn word-button [space]
  (let [selected-word (re-frame/subscribe [:selected-word])
        player-number (re-frame/subscribe [:player-number])
        hint (re-frame/subscribe [:hint])
        other-player-number (- 3 @player-number)
        word (key space)
        turn (re-frame/subscribe [:turn])
        your-turn? (or (= @player-number @turn)
                       (nil? @turn))]
    [re-com/button
     :label word
     :disabled? (or
                 ;; your turn = you're giving hint
                 your-turn?
                 ;; collapsed = already resolved
                 (not (:superposition (val space)))
                 ;; you already touched it
                 (some #{@player-number} (:touched-by (val space)))
                 ;; hint not ready yet
                 (nil? @hint))
     :class (clojure.string/join
             " "
             ["space"
              (if (= word @selected-word) "active")
              (if (and your-turn?
                       (not (:superposition (val space))))
                "collapsed")
              (cond
                (not (:superposition (val space))) (:colors (val space))
                your-turn? (nth (:colors (val space)) (- @player-number 1))
                :default (nth (:colors (val space)) (- 2 @player-number)))
              (if (or (:superposition (val space))
                      (= "w" (:colors (val space))))
                (clojure.string/join
                 " " (map #(cond
                             (= @player-number %) "t-you"
                             (= other-player-number %) "t-them")
                          (:touched-by (val space)))))])
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
  (let [new-hint (re-frame/subscribe [:hint-input])
        hint (re-frame/subscribe [:hint])]
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
                   :validation-regex #"^[0-9]*$"
                   :width "5em" ; number input, only need it to be as
                                ; big as the placeholder text.
                   :on-change #(re-frame/dispatch [:hint-input-changed {:count %}])]
                  [re-com/button
                   :label "Submit"
                   :disabled? (not (nil? @hint))
                   :on-click #(do (re-frame/dispatch [:game/hint])
                                  (re-frame/dispatch [:hint-input-changed {:word "" :count ""}]))]]])))

(defn touch-buttons []
  (let [hint (re-frame/subscribe [:hint])]
    [re-com/h-box
     :gap "0.5em"
     :children [[re-com/button
                 :label "Touch"
                 :disabled? (nil? @hint)
                 :on-click #(re-frame/dispatch [:game/touch])]
                [re-com/button
                 :label "Pass"
                 :disabled? (nil? @hint)
                 :on-click #(re-frame/dispatch [:game/pass])]]]))

(defn action-row []
  (let [turn (re-frame/subscribe [:turn])
        player-number (re-frame/subscribe [:player-number])]
    (cond
      (nil? @turn) [re-com/button
                    :label "Claim First Turn"
                    :on-click #(re-frame/dispatch [:game/claim])]
      (= @turn @player-number) [hint-input]
      :default [touch-buttons])))

(defn inner-game-panel []
  [re-com/v-box
   :gap "1em"
   :align :center
   :children [[hint-bar] [game-board] [action-row] [info-bar]]])

(defn game-over-overlay []
  (let [won (re-frame/subscribe [:won])]
    (fn []
      (cond
        (nil? @won) [:div ""]
        (= true @won) [re-com/modal-panel
                       :child [:div "Congratulon! You are super player!"]]
        :default [:div {:id "you-died-container"}
                  [:div {:id "you-died-overlay"}]
                  [:div {:id "you-died"} [:div "YOU DIED"]]]))))

(defn game-panel []
  (let [won (re-frame/subscribe [:won])]
    (fn []
      [re-com/v-box
       :height "100%"
       :width "100%"
       :align :center
       :justify :center
       :children [[inner-game-panel] [game-over-overlay]]])))

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
