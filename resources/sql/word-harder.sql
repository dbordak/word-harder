-- :name -insert-word :!
insert into words (word, list) values (:word, :list)

-- :name -insert-words :! :n
insert into words (word, list)
values :tuple*:words

-- :name -list-words :*
select word from words

-- :name -list-words-from :*
select word from words where list in (:v*:lists)
