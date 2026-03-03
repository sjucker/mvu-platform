create table shareable_link
(
    id          bigserial
        constraint pk__shareable_link primary key,
    uuid        varchar(36) not null
        constraint uq__shareable_link unique,
    description varchar(1024),
    valid_until date        not null
);

create table shareable_link_komposition
(
    id                bigserial
        constraint pk__shareable_link_noten primary key,
    shareable_link_id bigint not null,
    komposition_id    bigint not null,
    constraint fk__shareable_link_komposition_link foreign key (shareable_link_id) references shareable_link (id),
    constraint fk__shareable_link_komposition_komp foreign key (komposition_id) references komposition (id)
);

create table shareable_link_instrument
(
    id                bigserial
        constraint pk__shareable_link_instrument primary key,
    shareable_link_id bigint       not null,
    instrument        varchar(255) not null,
    constraint fk__shareable_link_instrument_link foreign key (shareable_link_id) references shareable_link (id)
);
