alter table exchange_history
    add column min_price_exchange varchar(16);
alter table exchange_history
    add column increment_step integer;

