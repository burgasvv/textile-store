
--liquibase formatted sql

--changeset burgasvv:1
create table if not exists category (
    id uuid default gen_random_uuid() unique not null ,
    name varchar unique not null
);

--changeset burgasvv:2
create table if not exists product (
    id uuid default gen_random_uuid() unique not null ,
    category_id uuid references category(id) on delete set null on update cascade ,
    name varchar unique not null ,
    description text unique not null ,
    price decimal not null default 0 check ( price >= 0 )
);

--changeset burgasvv:3
create table if not exists product_image (
    product_id uuid references product(id) on delete cascade on update cascade ,
    image_id uuid references image(id) on delete cascade on update cascade ,
    primary key (product_id, image_id)
);

--changeset burgasvv:4
begin ;
insert into category(id, name) values ('8983ee7e-2818-4c22-bbb6-5d397ec592bc', 'Ковры');
insert into category(id, name) values ('809750a1-18b9-4cd3-bc41-94c30f66a34c', 'Кошельки');
insert into category(id, name) values ('abcff599-dc00-4408-b4f7-6036923f6ff3', 'Сумки');
commit ;

--changeset burgasvv:5
begin ;
insert into product(id, category_id, name, description, price)
values ('0c5874e5-a184-4767-b447-92a6b11e2b14', '8983ee7e-2818-4c22-bbb6-5d397ec592bc', 'Новый Ковер',
        'Описание Нового Ковра', 7499.99);
commit ;