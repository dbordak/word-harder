(ns word-harder.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com]))


;; home

(defn home-title []
  [re-com/title
   :label "Words 2: Word Harder"
   :level :level1])

(defn new-game-button []
  [re-com/hyperlink-href
   :label "New Game"
   :class "btn btn-default"
   :href "#/game"])

(defn join-game-button []
  [re-com/hyperlink-href
   :label "Join Game"
   :class "btn btn-default"
   :href "#/game"])

(defn button-row []
  [re-com/h-box
   :gap "0.5em"
   :children [[new-game-button] [join-game-button]]])

(defn home-panel []
  [re-com/v-box
   :gap "1em"
   :children [[home-title] [button-row]]])


;; about

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

(defn word-button [word]
  (let [selected-word (re-frame/subscribe [:selected-word])]
    [re-com/radio-button
     :label word
     :value word
     :model selected-word
     :style {:visibility "hidden"}
     :label-class (str "btn btn-default"
                       (if (= word @selected-word)
                         " active" ""))
     :on-change #(re-frame/dispatch [:selected-word word])]))

(defn game-board []
  (let [word-list (re-frame/subscribe [:word-list])]
    (fn []
      [re-com/h-box
       :gap "0.2mm"
       :children (doall (map word-button @word-list))])))

(defn hint-input []
  (let [new-hint (re-frame/subscribe [:hint-input])]
    (fn []
      [re-com/h-box
       :gap "0.5em"
       :children [[re-com/input-text
                   :placeholder "Hint"
                   :model (:text @new-hint)
                   :on-change #(re-frame/dispatch [:hint-input-changed {:text %}])]
                  [re-com/input-text
                   :placeholder "Count"
                   :model (:count @new-hint)
                   :width "5em" ; number input, only need it to be as
                                ; big as the placeholder text.
                   :on-change #(re-frame/dispatch [:hint-input-changed {:count %}])]
                  [re-com/button
                   :label "Submit"
                   :on-click #(do (re-frame/dispatch [:hint-input-changed {:text "" :count ""}]))]]])))

(defn game-panel []
  [re-com/v-box
   :gap "1em"
   :children [[top-bar] [game-board] [hint-input]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :game-panel [game-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [re-com/v-box
       :height "100%"
       :children [[panels @active-panel]]])))
