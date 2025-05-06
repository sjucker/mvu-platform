alter table login
    add column document_permission varchar(255) not null default 'NONE';

create table document
(
    id          bigserial
        constraint pk__document primary key,
    name        varchar(255) not null,
    file_type   varchar(50)  not null,
    uploaded_by varchar(255) not null,
    uploaded_at timestamp    not null default current_timestamp,
    description text
);
