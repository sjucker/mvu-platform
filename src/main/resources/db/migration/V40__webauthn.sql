-- tables used by Spring Security's JdbcPublicKeyCredentialUserEntityRepository and JdbcUserCredentialRepository
create table user_entities
(
    id           varchar(1000) not null
        constraint pk__user_entities primary key,
    name         varchar(255)  not null
        constraint uq__user_entities__name unique
        constraint fk__user_entities__login references login (email) on update cascade on delete cascade,
    display_name varchar(255)
);

create table user_credentials
(
    credential_id                varchar(1000) not null
        constraint pk__user_credentials primary key,
    user_entity_user_id          varchar(1000) not null
        constraint fk__user_credentials__user_entities references user_entities (id) on delete cascade,
    public_key                   bytea         not null,
    signature_count              bigint,
    uv_initialized               boolean,
    backup_eligible              boolean       not null,
    authenticator_transports     varchar(1000),
    public_key_credential_type   varchar(100),
    backup_state                 boolean       not null,
    attestation_object           bytea,
    attestation_client_data_json bytea,
    created                      timestamp,
    last_used                    timestamp,
    label                        varchar(1000) not null
);

create index ix__user_credentials__user_entity_user_id on user_credentials (user_entity_user_id);
