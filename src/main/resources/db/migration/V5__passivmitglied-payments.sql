alter table passivmitglied_payment
    add column created_at timestamp not null default now();
alter table passivmitglied_payment
    add column created_by varchar(255) not null default 'System';

alter table passivmitglied
    add column external_id bigint not null default 1;
