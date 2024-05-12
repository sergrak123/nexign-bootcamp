--roles firstly
insert into public.roles (id, name)
values (1, 'USER');
insert into public.roles (id, name)
values (2, 'ADMIN');

--users
insert into public.users (password, username)
values ('$2a$10$D5joJBZNROqKdCvbYZgSbeDdpAef9J.oE4BOK8.m10zC/faVCB.MC', '79079765785');
insert into public.users (password, username)
values ('$2a$10$eAWiIlw7vw4K1CdhCdy7buPorXmXoqLpvaMc1pfxmvySQxHR2.klG', 'admin');

--user_roles
insert into public.user_roles (role_id, user_id)
values (1, 1);
insert into public.user_roles (role_id, user_id)
values (2, 2);
