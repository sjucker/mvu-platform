-- WebAuthn credentials (passkeys) of a login, used by the app to sign in without a password
create table passkey_credential
(
    id              bigserial
        constraint pk__passkey_credential primary key,
    fk_login        bigint       not null
        references login (id) on delete cascade,
    credential_id   bytea        not null
        constraint uq__passkey_credential_credential_id unique,
    user_handle     bytea        not null,
    public_key_cose bytea        not null,
    signature_count bigint       not null,
    name            varchar(255) not null,
    created_at      timestamp    not null,
    last_used_at    timestamp    null
);

create index ix__passkey_credential_fk_login on passkey_credential (fk_login);
create index ix__passkey_credential_user_handle on passkey_credential (user_handle);

-- pending registration/assertion requests (single use, short-lived), the API is stateless
create table passkey_challenge
(
    id           uuid
        constraint pk__passkey_challenge primary key,
    fk_login     bigint    null
        references login (id) on delete cascade,
    request_json text      not null,
    expires_at   timestamp not null
);
