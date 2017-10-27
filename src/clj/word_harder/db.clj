(ns word-harder.db
  (:require [hugsql.core :as hugsql]))

(def db {:subprotocol "h2"
         :subname "./word-harder.h2"})

(hugsql/def-db-fns "sql/word-harder.sql")

(defn insert-word [word]
  (-insert-word db {:word word}))

(defn insert-words [words]
  (-insert-words db {:words words}))

(defn list-words []
  (map :word (-list-words db)))

(defn list-words-from [lists]
  (map :word (-list-words-from db {:lists lists})))

(defn list-word-categories []
  (map :list (-list-word-categories db)))

(defn create-game [player1 board]
  ((keyword "scope_identity()")
   (-create-game db {:p1 player1
                     :board board})))

(defn set-player [id player-number player]
  (let [transaction-map {:id id :player player}]
    (cond
      (= player-number 1) (-set-player-1 db transaction-map)
      (= player-number 2) (-set-player-2 db transaction-map))))

(defn set-turn [id player]
  (-set-turn db {:id id
                 :turn player}))

(defn next-turn [id]
  (-next-turn db {:id id}))

(defn advance-turn [id]
  (-advance-turn db {:id id}))

(defn decrement-hints [id]
  (-decrement-hints db {:id id}))

(defn decrement-fails [id]
  (-decrement-fails db {:id id}))

(defn set-hint [id hint]
  (-set-hint db {:id id
                 :hint hint}))

(defn update-board [id board]
  (-update-board db {:id id
                     :board board}))

(defn game-over [id won?]
  (-game-over db {:id id
                  :won won?}))

(defn get-game [id]
  (-get-game db {:id id}))

(defn get-games-by-player [player]
  (-get-games-by-player db {:player player}))
