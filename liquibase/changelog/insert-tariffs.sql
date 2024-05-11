insert into public.call_cost (external_cost, internal_cost, call_cost_type)
values (0.00, 0.00, 2);
insert into public.call_cost (external_cost, internal_cost, call_cost_type)
values (2.50, 1.50, 1);
--
insert into public.tariff (benefit_minutes, subscription_fee, in_call_cost_type, out_call_cost_type, name, tariff)
values (0, 0.00, 2, 1, 'Классика', 'PER_MINUTE');
insert into public.tariff (benefit_minutes, subscription_fee, in_call_cost_type, out_call_cost_type, name, tariff)
values (50, 100.00, 2, 1, 'Помесячный', 'MONTHLY');

