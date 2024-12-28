alter table passivmitglied
    add column plz          varchar(10) not null default '',
    add column strasse_nr   varchar(5)  not null default '',
    add column country_code varchar(2)  not null default 'CH'
