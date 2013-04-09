# --- !Ups

create table file_entry (
  id                        SERIAL PRIMARY KEY,
  name                      varchar(255) not null,
  signature                 varchar(255),
  size                      double,
  root                      varchar(255),
  last_modified             timestamp

);



# --- !Downs

drop table if exists file_entry;


