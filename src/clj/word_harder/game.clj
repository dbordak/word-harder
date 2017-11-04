(ns word-harder.game
  (:require [word-harder.db :as db]))

;; Color 1 is used on turn 1, i.e. when player 1 gives the hint and
;; player 2 touches.
(defn create-key [& [color-map]]
  (let [black (or (:black color-map) 1)
        black-green (or (:black-green color-map) 1)
        black-white (or (:black-white color-map) 1)
        green (or (:green color-map) 3)
        green-white (or (:green-white color-map) 5)
        white (or (:white color-map) 7)]
    (apply concat
           (concat
            (repeat black '(["b" "b"]))
            (repeat black-green '(["b" "g"] ["g" "b"]))
            (repeat black-white '(["b" "w"] ["w" "b"]))
            (repeat green '(["g" "g"]))
            (repeat green-white '(["g" "w"] ["w" "g"]))
            (repeat white '(["w" "w"]))))))

(defn create-board [& {:keys [wordlists key custom-wordlist]
                       :or {key (create-key)}}]
  (zipmap (take (count key)
                (shuffle (if custom-wordlist
                           (distinct custom-wordlist)
                           (db/list-words wordlists))))
          (map #(hash-map :colors % :touched-by nil :superposition true) key)))

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
    ;; Touching a previously-untouched white space is the only case
    ;; which does not collapse superposition.
    (if (and (= color "w") (empty? (:touched-by space)))
      new-space
      (assoc new-space
             :colors color
             :superposition false))))

(defn check-for-victory [board]
  (every? #(if (= true (:superposition (val %)))
             (not (some #{"g"} (:colors (val %)))) true) board))

(defn check-for-completion [board player]
  (every? #(if (= true (:superposition (val %)))
             (not= "g" (nth (:colors (val %)) (- player 1))) true) board))

;; DB helpers

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
