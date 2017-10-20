-- :name create-game-table :!
create table games (
  id          bigint auto_increment,
  won         boolean,
  hints       integer not null default 9,
  fails       integer not null default 9,
  turn        integer,
  hint        varchar(50),
  p1          varchar(40),
  p2          varchar(40),
  board       other
)

-- :name drop-game-table :!
drop table games

-- :name create-word-table :!
create table words (
  word  varchar(40),
  list  varchar(40)
)

-- :name drop-word-table :!
drop table words

-- I'm not actually using a table for spaces (I'm storing them as
-- serialized objects inside the games table) but if I did, it'd
-- probably look like this:

-- :name create-space-table :!
create table spaces (
  game_id     bigint,
  word        varchar(40),
  color_p1    varchar(1),
  color_p2    varchar(1),
  touched_p1  boolean not null default false,
  touched_p2  boolean not null default false
)

-- name drop-space-table :!
drop table spaces
