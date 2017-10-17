(ns user
  (:require [word-harder.db :refer :all])
  (:require [hugsql.core :as hugsql])
  (:require [clj-yaml.core :as yaml]))

(hugsql/def-db-fns "sql/migrations.sql")

(defn migrate []
  (create-game-table db)
  (create-word-table db)
  ;;TODO: import from YAML
  (let [word-list (yaml/parse-string (slurp "resources/dictionary.yml"))]
    (doseq [sublist word-list]
      (insert-words db
                    {:words
                     (map #(list % (name (first sublist)))
                          (last sublist))}))))

(defn drop-all []
  (drop-game-table db)
  (drop-word-table db))
