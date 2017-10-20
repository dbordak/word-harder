(ns word-harder.db
  (:require [hugsql.core :as hugsql]))

(def db {:subprotocol "h2"
         :subname "./word-harder.h2"})

(hugsql/def-db-fns "sql/word-harder.sql")

(defn insert-word [map]
  (-insert-word db map))

(defn insert-words [map]
  (-insert-words db map))

(defn list-words []
  (distinct (-list-words db)))

(defn list-words-from [map]
  (distinct (-list-words-from db map)))

(defn create-game [player1 board]
  ((keyword "scope_identity()")
   (-create-game db {:p1 player1
                     :board board})))

(defn init-game [id player2]
  (-init-game db {:id id
                  :p2 player2}))

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
