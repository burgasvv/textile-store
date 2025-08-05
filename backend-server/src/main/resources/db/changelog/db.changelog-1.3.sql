
--liquibase formatted sql

--changeset burgasvv:1
create table if not exists bucket (
    id uuid default gen_random_uuid() unique not null ,
    identity_id uuid unique references identity(id) on delete set null on update cascade ,
    cost decimal default 0 check ( cost >= 0 ) not null
);

--changeset burgasvv:2
create table if not exists bucket_product (
    bucket_id uuid references bucket(id) on delete cascade on update cascade ,
    product_id uuid references product(id) on delete cascade on update cascade ,
    amount bigint not null check ( amount >= 0 ) ,
    primary key (bucket_id, product_id)
);

--changeset burgasvv:3
begin ;
insert into bucket(id, identity_id, cost) values ('b7e373be-9205-46f8-b5db-8f86d9b1b23b', 'f80a7113-d708-4058-8c80-42c42c1d691d', 0.0);
commit ;

--changeset burgasvv:4
alter table if exists bucket_product add column cost bigint default 0 check ( cost >= 0 );
alter table if exists bucket_product alter column cost set not null ;

--changeset burgasvv:5
alter table if exists bucket_product drop column cost ;
alter table if exists bucket_product add column cost decimal default 0 check ( cost >= 0 );
alter table if exists bucket_product alter column cost set not null ;