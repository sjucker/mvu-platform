create table instrument_permission
(
    fk_login   bigint       not null,
    instrument varchar(255) not null,
    constraint pk__instruments_permission primary key (fk_login, instrument)
);
