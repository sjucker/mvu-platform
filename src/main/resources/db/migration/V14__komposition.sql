alter table komposition
    drop column duration_in_seconds;

alter table komposition
    add column format varchar(255) not null default 'KONZERTMAPPE';
