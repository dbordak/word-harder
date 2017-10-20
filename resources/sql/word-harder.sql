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

-- :name -set-turn :!
update games
set turn = :turn
where id = :id

-- :name -next-turn :!
update games
set turn = 3 - turn,
hints = hints - 1,
hint = null
where id = :id

-- :name -advance-turn :!
update games
set hints = hints - 1,
hint = null
where id = :id

-- :name -decrement-hints :!
update games
set hints = hints - 1
where id = :id

-- :name -decrement-fails :!
update games
set fails = fails - 1
where id = :id

-- :name -set-hint :!
update games
set hint = :hint
where id = :id

-- :name -update-board :!
update games
set board = :board
where id = :id

-- :name -game-over :!
update games
set won = :won
where id = :id

-- :name -get-game :? :1
select * from games where id = :id
