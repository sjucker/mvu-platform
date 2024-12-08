create table login
(
    id               bigserial
        constraint pk__login primary key,
    email            varchar(255)
        constraint uq__login_email unique,
    name             varchar(255) not null,
    password         varchar(255) not null,
    active           boolean      not null,
    last_login       timestamp    null,
    users_permission varchar(255) not null default 'NONE'
);
