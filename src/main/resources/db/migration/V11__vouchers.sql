create table voucher
(
    id          bigserial
        constraint pk__voucher primary key,
    code_prefix varchar(255) not null,
    description varchar(255) not null,
    valid_until date         not null
);
