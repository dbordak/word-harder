-- :name -insert-word :!
insert into words (word, list) values (:word, :list)

-- :name -insert-words :! :n
insert into words (word, list)
values :tuple*:words

-- :name -list-words :*
select word from words

-- :name -list-words-from :*
select word from words where list in (:v*:lists)

-- :name -create-game :i!
insert into games (p1, board) values (:p1, :board)

-- :name -init-game :!
update games
set p2 = :p2
where id = :id

-- :name -get-game :? :1
select * from games where id = :id
