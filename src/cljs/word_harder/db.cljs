(ns word-harder.db)

(def default-db
  {;; UI and Inputs
   :game-id-input ""
   :wordlists []
   :custom-game-form {:tiles {:black "1"
                              :white "7"
                              :green "3"
                              :black-green "1"
                              :black-white "1"
                              :green-white "5"}
                      :wordlists (set [])
                      :hints "9"
                      :mistakes "9"}
   :hint-input {:word ""
                :count ""}
   :selected-word ""
   ;; Clientside game info
   :player-number nil
   :uid nil
   ;; Serverside game info; full push done after every serverside event.
   :game {:id nil
          :won nil
          :turn nil
          :hints 9
          :fails 9
          :hint nil
          :p1 nil
          :p2 nil
          :board {}}})
