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
    (into (sorted-map)
          (zipmap
           (map :word words)
           (map #(hash-map :colors % :touched-by nil :superposition true) key)))))

;; i have no idea why i need the = true check.
;; look at this:

;; word-harder.server=> (:superposition s)
;; false
;; word-harder.server=> (= false (:superposition s))
;; true
;; word-harder.server=> (if (:superposition s) 'a 'b)
;; a
;; word-harder.server=> (if false 'a 'b)
;; b

;; what the fuck

;; i'm guessing it's something to do with jdbc + this:
;; https://stackoverflow.com/questions/18676956/boolean-false-in-clojure

(defn hide-space [space player]
  (if (= true (:superposition space))
    (if (some #{player} (:touched-by space))
      space
      (let [other-player-idx (- 2 player)]
        (assoc-in space [:colors other-player-idx] "u")))
    space))

(defn hide-board [board player]
  (reduce-kv (fn [m k v] (assoc m k (hide-space v player)))
             (sorted-map) board))

(defn touched-color [space player]
  "Get the color of the SPACE if touched by PLAYER."
  (let [colors (:colors space)
        idx (- 2 player)]
    (nth colors idx)))

(defn touched-space [space player]
  "Get the new state of SPACE after being touched by PLAYER"
  (let [new-space (assoc space :touched-by
                         (cons player (:touched-by space)))
        color (touched-color space player)]
    (cond
      (= color "b") (assoc new-space
                           :colors "b"
                           :superposition false)
      (= color "g") (assoc new-space
                           :colors "g"
                           :superposition false)
      (= color "w") (if (empty? (:touched-by space))
                      new-space
                      (assoc new-space
                             :colors "w"
                             :superposition false)))))

(defn check-for-victory [board]
  (every? #(if (= true (:superposition (val %)))
             (not (some #{"g"} (:colors (val %)))) true) board))

(defn check-for-completion [board player]
  (every? #(if (= true (:superposition (val %)))
             (not= "g" (nth (:colors (val %)) (- player 1))) true) board))

;; DB helpers

(defn create-game [player1]
  (db/create-game player1 (create-board)))

(defn next-turn [game-info]
  (if (not (check-for-completion (:board game-info)
                                 (- 3 (:turn game-info))))
    (db/next-turn (:id game-info))
    (db/advance-turn (:id game-info))))

(defn decrement-fails [game-info]
  (if (> (:fails game-info) 0)
    (db/decrement-fails (:id game-info))
    (db/decrement-hints (:id game-info))))

(defn touch-space! [game-info space]
  (let [player (- 3 (:turn game-info))
        color (touched-color space player)]
    (cond
      (= color "b") (db/game-over (:id game-info) false)
      (= color "w") (do (decrement-fails game-info)
                        (next-turn game-info)))
    (touched-space space player)))

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

(defn touch-space-in-game! [game-info word]
  (let [board (:board game-info)
        new-board (assoc board word (touch-space! game-info (get board word)))]
    ;; end game if there are no more greens for either player
    (if (check-for-victory new-board)
      (db/game-over (:id game-info) true)
      ;; automatically end turn if there are no more greens for this toucher
      (if (check-for-completion new-board (:turn game-info))
        (db/next-turn (:id game-info))))
    (assoc game-info :board new-board)))

(defn set-turn [game-info player-uuid]
  (let [player (player-number game-info player-uuid)]
    (when (not= player 0)
      (db/set-turn (:id game-info) player))))
