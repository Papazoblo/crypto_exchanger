create sequence if not exists cr_schema.asset_balance_seq_id;
create table cr_schema.asset_balance
(
    id        bigint not null default nextval('cr_schema.asset_balance_seq_id'),
    usdt_info varchar(10),
    eth_info  varchar(10),
    create_at timestamp,
    primary key (id)
);