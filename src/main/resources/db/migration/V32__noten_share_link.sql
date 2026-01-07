create table if not exists noten_share_link
(
    id           bigserial primary key,
    fk_konzert   bigint      not null references konzert (id) on delete cascade,
    instrument   varchar(64) not null,
    token        uuid        not null unique,
    expires_at   timestamp   null,
    active       boolean     not null default true,
    created_at   timestamp   not null default now()
);

create index if not exists idx_noten_share_link_konzert_instrument
    on noten_share_link (fk_konzert, instrument);
