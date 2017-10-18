(ns word-harder.db)

(def default-db
  {:game-id-input ""
   :hint-input {:word ""
                :count ""}
   :selected-word ""
   :player-number nil
   :game {:id nil
          :over false
          :winner nil
          :turn nil
          :hint nil
          :p1 nil
          :p2 nil
          :board []}})
