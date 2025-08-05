
--liquibase formatted sql

--changeset burgasvv:1
create table if not exists bill (
    id uuid default gen_random_uuid() unique not null ,
    identity_id uuid references identity(id) on delete set null on update cascade ,
    cost decimal default 0 check ( cost >= 0 ) not null
);

--changeset burgasvv:2
create table if not exists bill_product (
    bill_id uuid references bill(id) on delete cascade on update cascade ,
    product_id uuid references product(id) on delete cascade on update cascade ,
    primary key (bill_id, product_id)
);

--changeset burgasvv:3
alter table if exists bill_product add column amount bigint default 0 check ( amount >= 0 );
alter table if exists bill_product alter column amount set not null ;

--changeset burgasvv:4
alter table if exists bill_product add column cost bigint default 0 check ( cost >= 0 );
alter table if exists bill_product alter column cost set not null ;

--changeset burgasvv:5
alter table if exists bill_product drop column cost ;
alter table if exists bill_product add column cost decimal default 0 check ( cost >= 0 );
alter table if exists bill_product alter column cost set not null ;