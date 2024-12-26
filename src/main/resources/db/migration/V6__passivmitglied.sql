alter table passivmitglied
    add column anrede varchar(255) not null default '';
alter table passivmitglied
    add column uuid varchar(255) not null default '';

create table passivmitglied_voucher
(
    id                bigserial
        constraint pk__passivmitglied_voucher primary key,
    fk_passivmitglied bigint       not null references passivmitglied (id) on delete cascade,
    code              varchar(255) not null,
    description       varchar(255) not null,
    valid_until       date         not null,
    redeemed_at       timestamp,
    redeemed_by       varchar(255)
);
