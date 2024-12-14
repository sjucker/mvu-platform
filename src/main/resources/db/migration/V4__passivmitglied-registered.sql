alter table passivmitglied
    add column registered_at timestamp not null default now();
