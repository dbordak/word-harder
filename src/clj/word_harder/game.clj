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

;; Color 1 is used on turn 1, i.e. when player 1 gives the hint and
;; player 2 touches.
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
    (shuffle (map #(assoc %1 :colors %2 :touched-by nil :superposition true)
                  words key))))

(defn hide-space [space player]
  (if (:superposition space)
    (if (= player (:touched-by space))
      space
      (let [other-player-idx (- 2 player)]
        (assoc-in space [:colors other-player-idx] "u")))
    space))

(defn hide-board [board player]
  (map #(hide-space % player) board))

(defn touch-space [word-object player]
  (let [colors (:colors word-object)
        idx (- 2 player)
        touched-by (cons player (:touched-by word-object))]
    (cond
      (= (nth colors idx) "b") word-object ;;TODO: Game over
      (= (nth colors idx) "g") (assoc word-object
                                      :colors "g"
                                      :superposition false
                                      :touched-by touched-by)
      (= (nth colors idx) "w") (if (empty? (:touched-by word-object))
                                 ;;TODO: decrement fails
                                 (assoc word-object
                                        :touched-by touched-by)
                                 (assoc word-object
                                        :colors "w"
                                        :superposition false
                                        :touched-by touched-by)))))

(defn touch-space-in-board [board player word]
  (map #(if (= word (:word %))
          (touch-space % player) %) board))

(defn check-for-victory [board]
  (every? #(if (:superposition %)
             (not (some #{"g"} (:colors %))) true) board))

;; DB helpers

(defn create-game [player1]
  (db/create-game player1 (create-board)))

;; Game Info functions

(defn player-number [game-info player-uuid]
  (cond
    (= player-uuid (:p1 game-info)) 1
    (= player-uuid (:p2 game-info)) 2
    :else 0))

(defn hide-game-info [game-info player-uuid]
  (let [board (:board game-info)
        player (player-number game-info player-uuid)]
    (when (not= player 0)
      (assoc game-info :board (hide-board board player)))))

(defn touch-space-in-game [game-info word]
  (let [board (:board game-info)
        player (- 3 (:turn game-info))]
    (assoc game-info :board (touch-space-in-board board player word))))

(defn set-turn [game-info player-uuid]
  (let [player (player-number game-info player-uuid)]
    (when (not= player 0)
      (db/set-turn (:id game-info) player))))

(defn set-winner [game-info player-uuid]
  (let [player (player-number game-info player-uuid)]
    (when (not= player 0)
      (db/set-winner (:id game-info) player))))
