alter table login
    add column konzerte_permission varchar(255) not null default 'NONE';

create table konzert
(
    id          bigserial
        constraint pk__konzert primary key,
    name        varchar(255) not null,
    datum       date         not null,
    zeit        time         not null,
    location    varchar(255),
    description text
);

create table konzert_entry
(
    id             bigserial
        constraint pk__konzert_entry primary key,
    fk_konzert     bigint not null references konzert (id) on delete cascade,
    index          int    not null,
    fk_komposition bigint references komposition (id),
    description    varchar(255)
);

