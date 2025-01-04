alter table login
    add column repertoire_permission varchar(255) not null default 'NONE';

create table repertoire
(
    id         bigserial
        constraint pk__repertoire primary key,
    type       varchar(255) not null,
    created_at timestamp    not null,
    details    text
);

create table repertoire_entry
(
    id             bigserial
        constraint pk__repertoire_entry primary key,
    fk_repertoire  bigint        not null references repertoire (id),
    fk_komposition bigint        not null references komposition (id),
    number         decimal(3, 1) null
);
