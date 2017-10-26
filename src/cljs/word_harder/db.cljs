(ns word-harder.db)

(def default-db
  {:game-id-input ""
   :hint-input {:word ""
                :count ""}
   :selected-word ""
   :player-number nil
   :uid nil
   :game {:id nil
          :won nil
          :turn nil
          :hints 9
          :fails 9
          :hint nil
          :p1 nil
          :p2 nil
          :board {}}})
