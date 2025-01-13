alter table passivmitglied
    rename to supporter;
alter table supporter
    add column type varchar(255) not null default 'PASSIVMITGLIED';

alter table passivmitglied_payment
    rename to supporter_payment;
alter table supporter_payment
    rename column fk_passivmitglied to fk_supporter;

alter table passivmitglied_voucher
    rename to supporter_voucher;
alter table supporter_voucher
    rename column fk_passivmitglied to fk_supporter;

alter table voucher
    add column type varchar(255) not null default 'PASSIVMITGLIED';

alter table login
    rename column passivmitglied_permission to supporter_permission;
