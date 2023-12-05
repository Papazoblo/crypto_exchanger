alter table exchange_history
    add column cancel_type varchar(50);

update exchange_history
set cancel_type = 'BY_PRICE_ADJUSTMENT'
where order_status = 'CANCELED';