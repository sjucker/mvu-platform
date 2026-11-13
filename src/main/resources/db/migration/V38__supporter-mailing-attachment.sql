-- optional attachment of a supporter mailing (only sent by email)
create table supporter_mailing_attachment
(
    fk_supporter_mailing bigint       not null
        constraint pk__supporter_mailing_attachment primary key
        references supporter_mailing (id) on delete cascade,
    file_name            varchar(255) not null,
    content_type         varchar(255) not null,
    data                 bytea        not null
);
