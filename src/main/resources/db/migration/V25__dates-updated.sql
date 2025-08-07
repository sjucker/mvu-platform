alter table event
    add column updated_at timestamp;
alter table event
    add column updated_by varchar(255);
