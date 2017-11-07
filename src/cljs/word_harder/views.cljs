(ns word-harder.views
  (:require [re-frame.core :as re-frame]
            [re-com.core :as re-com]
            [reagent.core :as reagent]))


;; home

(defn tile-form-row [name key tiles & [post-text]]
  [:div {:class "tile-form-row"}
   [:span name]
   [re-com/input-text
    :width "4.5em"
    :validation-regex #"^[0-9]*$"
    :model (key tiles)
    :on-change #(re-frame/dispatch [:tile-input-changed key %])]
   (if post-text
     [:span post-text])])

(defn tile-form [tiles]
  [re-com/v-box
   :align :center
   :children [[:h3 "Tiles"]
              [:div {:id "tile-form"}
               (tile-form-row "Black" :black tiles)
               (tile-form-row "White" :white tiles)
               (tile-form-row "Green" :green tiles)
               (tile-form-row "Black and Green" :black-green tiles "(per player)")
               (tile-form-row "Black and White" :black-white tiles "(per player)")
               (tile-form-row "Green and White" :green-white tiles "(per player)")]
              [:span "Total tiles: "
               (+ (js/parseInt (:black tiles))
                  (js/parseInt (:white tiles))
                  (js/parseInt (:green tiles))
                  (* 2 (js/parseInt (:black-green tiles)))
                  (* 2 (js/parseInt (:black-white tiles)))
                  (* 2 (js/parseInt (:green-white tiles))))]]])

;; Had to copy out these two functions from re-com since they're so
;; tightly bound to bootstrap.
(defn- check-clicked
  [selections item-id ticked? required?]
  (let [num-selected (count selections)
        only-item    (when (= 1 num-selected) (first selections))]
    (if (and required? (= only-item item-id))
      selections  ;; prevent unselect of last item
      (if ticked? (conj selections item-id) (disj selections item-id)))))

(defn wordlist-checkbox-renderer
  [item id-fn selections on-change disabled? label-fn required? as-exclusions?]
  (let [item-id (id-fn item)]
    [re-com/box
     :class "list-group-item compact"
     :attr {:on-click (re-com/handler-fn (when-not disabled?
                                           (on-change (check-clicked selections item-id (not (selections item-id)) required?))))}
     :child [re-com/checkbox
             :model (some? (selections item-id))
             :on-change #()
             :disabled? disabled?
             :label-class "checkable"
             :label-style {:padding-left "1.5em"}
             :label (label-fn item)]]))

(defn wordlist-form []
  (let [form-data (re-frame/subscribe [:custom-game-form])
        wordlists (re-frame/subscribe [:wordlists])]
    [re-com/v-box
     :align :center
     :children [[:h3 "Wordlists"]
                [re-com/selection-list
                 :choices (map #(hash-map :label %1 :id %1) @wordlists)
                 :model (:wordlists @form-data)
                 :item-renderer wordlist-checkbox-renderer
                 :on-change #(re-frame/dispatch [:wordlist-input-changed %])]
                [re-com/title
                 :label "- or -"
                 :level :level4
                 :style {:margin "1em 0"}]
                [:input {:type "file"
                         :on-change #(re-frame/dispatch
                                      [:parse-wordlist-file
                                       (-> % .-target .-files (aget 0) list)])}]]]))

(defn turns-form []
  (let [form-data (re-frame/subscribe [:custom-game-form])]
    [re-com/v-box
     :align :center
     :children [[:h3 "Turns"]
                [:div {:id "turns-form"}
                 [:div {:class "turns-form-row"}
                  [:span "Hints"]
                  [re-com/input-text
                   :width "4.5em"
                   :validation-regex #"^[0-9]*$"
                   :model (:hints @form-data)
                   :on-change #(re-frame/dispatch [:hints-input-changed %])]]
                 [:div {:class "turns-form-row"}
                  [:span "Mistakes"]
                  [re-com/input-text
                   :width "4.5em"
                   :validation-regex #"^[0-9]*$"
                   :model (:mistakes @form-data)
                   :on-change #(re-frame/dispatch [:mistakes-input-changed %])]]]]]))

(defn custom-game-form []
  (let [form-data (re-frame/subscribe [:custom-game-form])]
    (fn []
      [re-com/v-box
       :align :center
       :gap "0.5em"
       :children [[:h2 "Custom Game"]
                  [re-com/h-box
                   :gap "1em"
                   :children [(tile-form (:tiles @form-data))
                              [re-com/line]
                              [wordlist-form]
                              [re-com/line]
                              [turns-form]]]
                  [re-com/button
                   :label "Create Custom Game"
                   :on-click #(re-frame/dispatch [:game/create-custom])]]])))

(defn custom-game-modal []
  (let [show? (reagent/atom false)]
    (fn []
      [re-com/v-box
       :children [[re-com/button
                   :label "Custom"
                   :on-click #(do (re-frame/dispatch [:game/get-wordlists])
                                  (reset! show? true))]
                  (when @show?
                    [re-com/modal-panel
                     :backdrop-on-click #(reset! show? false)
                     :child [custom-game-form]])]])))

(defn new-game-box []
  [re-com/v-box
   :size "1"
   :align :center
   :children [[:h2 "New Game"]
              [re-com/h-box
               :gap "1em"
               :children [[re-com/button
                           :label "Standard"
                           :on-click #(re-frame/dispatch [:game/create])]
                          [custom-game-modal]]]]])

(defn join-game-box []
  (let [game-id-input (re-frame/subscribe [:game-id-input])]
    (fn []
      [re-com/v-box
       :size "1"
       :align :center
       :children [[:h2 "Join Game"]
                  [:form {:id "game-id-form"}
                   [re-com/input-text
                    :placeholder "Game ID"
                    :width "6em"
                    :model @game-id-input
                    :on-change #(re-frame/dispatch [:game-id-input-changed %])]
                   [re-com/button
                    :label "Join"
                    :style {:margin-left "1em"}
                    :on-click #(do (re-frame/dispatch [:game/join])
                                   (.preventDefault %))]]]])))

(defn repo-link []
  [re-com/hyperlink-href
   :label "Github"
   :href "https://www.github.com/dbordak/word-harder"])

(defn home-panel []
  [re-com/v-box
   :gap "1em"
   :align :center
   :width "30em"
   :children [[:h1 "Words 2: Word Harder"]
              [re-com/h-box
               :width "100%"
               :gap "1em"
               :children [[new-game-box]
                          [join-game-box]]]
              [repo-link]]])


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
          mistakes (re-frame/subscribe [:fails])
          game-id (re-frame/subscribe [:game-id])]
      [re-com/h-box
       :justify :between
       :width "100%"
       :children [[re-com/title
                   :label (str "Turns Remaining: " @turns)
                   :level :level3]
                  [re-com/title
                   :label (str "Game ID: " @game-id)
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
              (if (not (:superposition (val space)))
                (:colors (val space))
                (nth (:colors (val space)) (- (if your-turn?
                                                @player-number
                                                other-player-number) 1)))
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
                                  (re-frame/dispatch [:hint-input-changed
                                                      {:word "" :count ""}]))]]])))

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
