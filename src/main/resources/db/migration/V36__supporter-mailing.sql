create table supporter_mailing
(
    id                        bigserial
        constraint pk__supporter_mailing primary key,
    subject                   varchar(255) not null,
    mjml                      text         not null,
    html                      text,
    filter_types              varchar(255),
    filter_paid_within_months integer,
    filter_email              boolean      not null default true,
    filter_post               boolean      not null default true,
    created_at                timestamp    not null,
    created_by                varchar(255) not null,
    updated_at                timestamp,
    updated_by                varchar(255),
    sent_at                   timestamp,
    sent_by                   varchar(255)
);

create table supporter_mailing_recipient
(
    id                   bigserial
        constraint pk__supporter_mailing_recipient primary key,
    fk_supporter_mailing bigint      not null references supporter_mailing (id) on delete cascade,
    fk_supporter         bigint      not null references supporter (id) on delete cascade,
    channel              varchar(10) not null,
    sent_at              timestamp,
    error                text
);
