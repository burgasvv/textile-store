
--liquibase formatted sql

--changeset burgasvv:1
create table if not exists identity (
    id uuid default gen_random_uuid() unique not null ,
    username varchar unique not null ,
    password varchar not null ,
    email varchar unique not null ,
    phone varchar unique not null ,
    authority varchar not null ,
    image_id uuid references image(id) on delete set null on update cascade
);

--changeset burgasvv:2
alter table if exists identity add column enabled boolean ;
alter table if exists identity alter column enabled set not null ;

--changeset burgasvv:3
begin ;
insert into identity(id, username, password, email, phone, authority, image_id, enabled)
values ('f80a7113-d708-4058-8c80-42c42c1d691d','moscowa','$2a$10$mJWFtEQoBcM6TgODtoou5Oj7/OK9uNZGenttdpb4xFS.cgjdwV0bC',
        'moscowa@gmail.com','(952)-739-56-04','ADMIN',null, true);
commit ;