-- :name -insert-words :! :n
insert into words (word, list)
values :tuple*:words

-- :name -list-words :*
select distinct word from words

-- :name -list-words-from :*
select distinct word from words where list in (:v*:lists)

-- :name -list-word-categories :*
select distinct list from words



-- :name -create-game :i!
insert into games (p1, board) values (:p1, :board)

-- :name -set-player-1 :!
update games
set p1 = :player
where id = :id

-- :name -set-player-2 :!
update games
set p2 = :player
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

-- :name -get-games-by-player
select * from games
where won is null
and (p1 = :player
or p2 = :player)
