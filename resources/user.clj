(ns user
  (:require [word-harder.db :refer :all])
  (:require [hugsql.core :as hugsql])
  (:require [clj-yaml.core :as yaml]))

(hugsql/def-db-fns "sql/migrations.sql")

(defn import-dictionary [filepath]
  (let [word-list (yaml/parse-string (slurp filepath))]
    (doseq [sublist word-list]
      (insert-words (map #(list % (name (first sublist)))
                         (last sublist))))))

(defn drop-all []
  (drop-game-table db)
  (drop-word-table db))

(defn build-default-db []
  (create-game-table db)
  (create-word-table db)
  (import-dictionary "resources/dictionary.yml"))
