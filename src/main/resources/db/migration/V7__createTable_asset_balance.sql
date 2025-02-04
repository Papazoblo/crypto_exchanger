create sequence if not exists asset_balance_seq_id;
create table asset_balance
(
    id        bigint not null,
    usdt_info varchar(10),
    eth_info  varchar(10),
    create_at timestamp,
    primary key (id)
);