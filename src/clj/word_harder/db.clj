(ns word-harder.db
  (:require [hugsql.core :as hugsql]
            [config.core :refer [env]]
            [taoensso.carmine :as car]))

(def db (or (env :database-url)
            {:subprotocol "postgresql"
             :subname "word-harder"}))

(def redis-conn {:pool {}
                 :spec {:uri (or (env :redis-url)
                                 "redis://localhost:6379")}})
(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(hugsql/def-db-fns "sql/word-harder.sql")

(defn insert-words [words]
  (-insert-words db {:words words}))

(defn list-words [& [wordlists]]
  (map :word
       (if (or (nil? wordlists) (empty? wordlists))
         (-list-words db)
         (-list-words-from db {:lists wordlists}))))

(defn list-word-categories []
  (map :list (-list-word-categories db)))

(def random-characters (char-array "BCDFGHJKLMNPQRSTVWXZ"))
(defn generate-random-id []
  (apply str (take 4 (repeatedly #(rand-nth random-characters)))))

(defn get-unique-id []
  (let [id (generate-random-id)]
    (if (= 0 (wcar* (car/exists id)))
      id
      (get-unique-id))))

(defn create-game [player1 board & {:keys [hints mistakes]
                                    :or {hints 9 mistakes 9}} ]
  (let [game-id (get-unique-id)]
    (wcar* (car/set game-id "")
           (car/set (str game-id :p1) player1)
           (car/set (str game-id :p2) nil)
           (car/set (str game-id :board) board)
           (car/set (str game-id :hints) hints)
           (car/set (str game-id :mistakes) mistakes)
           (car/set (str game-id :won) nil)
           (car/set (str game-id :turn) nil)
           (car/set (str game-id :hint) nil)
           (car/set player1 game-id))
    game-id))

(defn set-player [id player-number player]
  (wcar* (car/set (str id ":p" player-number) player)
         (car/set player id)))

(defn set-turn [id player]
  (wcar* (car/set (str id :turn) player)))

(defn next-turn [id]
  (let [turn (wcar* (car/get (str id :turn)))]
    (if (not (nil? turn))
      (wcar* (car/set (str id :turn) (- 3 (Integer. turn)))
             (car/decr (str id :hints))
             (car/set (str id :hint) nil)))))

(defn advance-turn [id]
  (wcar* (car/decr (str id :hints))
         (car/set (str id :hint) nil)))

(defn decrement-hints [id]
  (wcar* (car/decr (str id :hints))))

(defn decrement-mistakes [id]
  (wcar* (car/decr (str id :mistakes))))

(defn set-hint [id hint]
  (wcar* (car/set (str id :hint) hint)))

(defn update-board [id board]
  (wcar* (car/set (str id :board) board)))

(defn game-over [id won?]
  (wcar* (car/set (str id :won) won?)))

(defn get-game [id]
  (let [[p1 p2 board hints mistakes won turn hint]
        (wcar* (car/get (str id :p1))
               (car/get (str id :p2))
               (car/get (str id :board))
               (car/get (str id :hints))
               (car/get (str id :mistakes))
               (car/get (str id :won))
               (car/get (str id :turn))
               (car/get (str id :hint)))]
    {:id id
     :p1 p1
     :p2 p2
     :board board
     :hints (Integer. hints)
     :mistakes (Integer. mistakes)
     :won won
     :turn (if (not (nil? turn)) (Integer. turn))
     :hint hint}))

(defn get-game-by-player [player]
  (let [game-id (wcar* (car/get player))]
    (get-game game-id)))

(defn get-games-by-player [player]
  (list (get-game-by-player player)))

(defn player-number [player]
  (let [game-id (wcar* (car/get player))
        [p1 p2] (wcar* (car/get (str game-id :p1))
                       (car/get (str game-id :p2)))]
    (cond
      (= player p1) 1
      (= player p2) 2
      :else nil)))
