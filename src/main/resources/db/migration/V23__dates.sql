alter table login
    add column event_permission varchar(255) not null default 'NONE';

create table event
(
    id                   bigserial
        constraint pk__event primary key,
    from_date            date         not null,
    from_time            time         null,
    to_date              date         null,
    to_time              time         null,
    approximately        boolean      not null,
    title                varchar(255) not null,
    description          text         null,
    location             varchar(255) null,
    interna              text         null,
    literature           text         null,
    type                 varchar(16)  not null,
    relevant_for_absenz  boolean      not null,
    relevant_for_website boolean      not null,
    created_at           timestamp    not null,
    created_by           varchar(255) not null,
    deleted_at           timestamp    null,
    next_version         bigint       null,
    constraint fk__event_version
        foreign key (next_version)
            references event (id)
);

create table absenz_status
(
    fk_login bigint      not null,
    fk_event bigint      not null,
    remark   text        null,
    status   varchar(50) not null,
    constraint pk__absenz_status primary key (fk_login, fk_event),
    constraint fk__absenz_status_login foreign key (fk_login) references login (id),
    constraint fk__absenz_status_event foreign key (fk_event) references event (id)
);
