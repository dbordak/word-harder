(ns word-harder.db)

(def default-db
  {:hint nil
   :hint-input {:text ""
                :count ""}
   :word-list ["apple" "banana" "truck"]
   :selected-word "banana"
   :user {:name ""
          :email ""}
   :chat {:enabled? true
          :ready? false
          :msg-input ""
          :msg-list (vector)}})
