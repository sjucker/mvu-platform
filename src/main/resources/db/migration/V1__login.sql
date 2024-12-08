create table login
(
    email            varchar(255)
        constraint pk__login primary key,
    name             varchar(255) not null,
    password         varchar(255) not null,
    active           boolean      not null,
    last_login       timestamp    null,
    users_permission varchar(255) not null default 'NONE'
);
