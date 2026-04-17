alter table login
    add column calendar_token varchar(36);
update login
set calendar_token = gen_random_uuid()::varchar
where calendar_token is null;
alter table login
    alter column calendar_token set not null;
alter table login
    add constraint uq__login__calendar_token unique (calendar_token);
