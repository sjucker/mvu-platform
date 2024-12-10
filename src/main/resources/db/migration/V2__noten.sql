alter table login
    add column noten_permission varchar(255) not null default 'NONE';


create table komposition
(
    id                bigserial
        constraint pk__komposition primary key,
    titel             varchar(255) not null,
    komponist         varchar(255),
    arrangeur         varchar(255),
    durationInSeconds int
);

create table noten
(
    id             bigserial
        constraint pk__noten primary key,
    fk_komposition bigint       not null references komposition (id) on delete cascade,
    instrument     varchar(255) not null,
    stimme         varchar(255),
    stimmlage      varchar(255)
);
