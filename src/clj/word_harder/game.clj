(ns word-harder.game
  (:require [word-harder.db :as db]))

(def black-black 1)
(def black-green 1)
(def green-black 1)
(def black-white 1)
(def white-black 1)
(def green-green 3)
(def green-white 5)
(def white-green 5)
(def white-white 7)

;; First color is for player 1 (i.e. what player 2 sees and must have
;; player 1 touch) and vice versa
(defn create-key []
  (concat (repeat black-black ["b" "b"])
          (repeat black-green ["b" "g"])
          (repeat green-black ["g" "b"])
          (repeat black-white ["b" "w"])
          (repeat white-black ["w" "b"])
          (repeat green-green ["g" "g"])
          (repeat green-white ["g" "w"])
          (repeat white-green ["w" "g"])
          (repeat white-white ["w" "w"])))

(defn create-board [& [wordlists]]
  (let [words (take 25 (shuffle
                        (if (nil? wordlists)
                            (db/list-words)
                          (db/list-words-from {:lists wordlists}))))
        key (create-key)]
    (map #(assoc %1 :colors %2 :touched-by '()) words key)))

(defn hide-space [space player]
  (if (some #{player} (:touched-by space))
    space
    (let [other-player-idx (- 2 player)]
      (assoc-in space [:colors other-player-idx] "u"))))

(defn hide-board [board player]
  (map #(hide-space % player) board))
