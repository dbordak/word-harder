-- :name create-game-table :!
create table games (
  id          serial primary key,
  over        boolean not null,
  winner      integer,
  turn        integer,
  hint        varchar(140),
  hint_count  integer,
  p1          varchar(40),
  p2          varchar(40),
  words       array
);

-- :name drop-game-table :!
drop table games;

-- :name create-word-table :!
create table words (
  word  varchar(40),
  list  varchar(40)
);

-- :name drop-word-table :!
drop table words;
