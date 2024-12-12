create table passivmitglied
(
    id                  bigserial
        constraint pk__passivmitglied primary key,
    vorname             varchar(255) not null,
    nachname            varchar(255) not null,
    strasse             varchar(255) not null,
    ort                 varchar(255) not null,
    email               varchar(255) not null unique,
    bemerkung           text,
    kommunikation_post  boolean      not null,
    kommunikation_email boolean      not null
);

create table passivmitglied_payment
(
    id                bigserial
        constraint pk__passivmitglied_payment primary key,
    fk_passivmitglied bigint        not null references passivmitglied (id) on delete cascade,
    datum             date          not null,
    amount            numeric(6, 2) not null,
    bemerkung         text
);
