-- clients
create table if not exists clients
(
    balance      numeric(38, 2),
    id           bigserial
        constraint clients_pkey
            primary key,
    phone_number varchar(255),
    tariff       varchar(255)
        constraint clients_tariff_check
            check ((tariff)::text = ANY
                   ((ARRAY ['PER_MINUTE'::character varying, 'MONTHLY'::character varying])::text[]))
);

alter table clients
    owner to postgres;

alter sequence clients_id_seq owner to postgres;
alter sequence clients_id_seq owned by clients.id;


-- tariff
create table if not exists tariff
(
    benefit_minutes    integer,
    subscription_fee   numeric(38, 2),
    in_call_cost_type  bigint,
    out_call_cost_type bigint,
    name               varchar(255),
    tariff             varchar(255) not null
        constraint tariff_pkey
            primary key
        constraint tariff_tariff_check
            check ((tariff)::text = ANY
                   ((ARRAY ['PER_MINUTE'::character varying, 'MONTHLY'::character varying])::text[]))
);

alter table tariff
    owner to postgres;


-- call_cost
create table call_cost
(
    external_cost  numeric(38, 2),
    internal_cost  numeric(38, 2),
    call_cost_type bigserial
        constraint call_cost_pkey
            primary key
);

alter table call_cost
    owner to postgres;


-- role
create table roles
(
    id   bigserial
        constraint roles_pkey
            primary key,
    name varchar(255)
        constraint roles_name_check
            check ((name)::text = ANY ((ARRAY ['USER'::character varying, 'ADMIN'::character varying])::text[]))
);

alter table roles
    owner to postgres;


--users(CRM)
create table users
(
    id       bigserial
        constraint users_pkey
            primary key,
    password varchar(255),
    username varchar(255)
);

alter table users
    owner to postgres;


--user_role
create table user_roles
(
    role_id bigint not null
        constraint fkh8ciramu9cc9q3qcqiv4ue8a6
            references roles,
    user_id bigint not null
        constraint fkhfh9dx7w3ubf1co1vdev94g3f
            references users
);

alter table user_roles
    owner to postgres;