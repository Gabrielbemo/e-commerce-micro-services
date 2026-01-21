create table if not exists category
(
    id UUID not null primary key,
    name varchar(255) not null,
    description varchar(255)
);

create table if not exists product
(
    id UUID not null primary key,
    name varchar(255) not null,
    description varchar(255),
    available_quantity double precision not null,
    price numeric(38, 2) not null,
    category_id UUID,
    constraint fk_category foreign key (category_id) references category(id)
);
