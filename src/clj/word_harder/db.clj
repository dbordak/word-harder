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

(defn create-game [map]
  ((keyword "scope_identity()") (-create-game db map)))

(defn init-game [map]
  (-init-game db map))

(defn get-game [map]
  (-get-game db map))
