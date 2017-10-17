(ns word-harder.db)

(def default-db
  {:hint nil
   :hint-input {:text ""
                :count ""}
   :board [{:word "apple"} {:word "banana"} {:word  "truck"}]
   :selected-word ""
   :team ""})
