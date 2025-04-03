alter table noten_pdf
    drop column instrument;

create table noten_pdf_assignment
(
    id           bigserial
        constraint pk__noten_pdf_assignment primary key,
    fk_noten_pdf bigint       not null,
    instrument   varchar(255) not null,
    stimme       varchar(255),
    constraint fk__noten_pdf_assignment foreign key (fk_noten_pdf) references noten_pdf (id) on delete cascade
);
