-- :name create-word-table :!
create table words (
  word  varchar(40),
  list  varchar(40)
)

-- :name drop-word-table :!
drop table words

-- :name -insert-words :! :n
insert into words (word, list)
values :tuple*:words

-- :name -list-words :*
select distinct word from words

-- :name -list-words-from :*
select distinct word from words where list in (:v*:lists)

-- :name -list-word-categories :*
select distinct list from words
