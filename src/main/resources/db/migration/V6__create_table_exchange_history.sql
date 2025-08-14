create sequence if not exists cr_schema.exchange_history_id_seq;
CREATE TABLE cr_schema.exchange_history
(
    id                     bigint PRIMARY KEY NOT NULL default nextval('cr_schema.exchange_history_id_seq'),
    order_id               bigint             NOT NULL,
    type                   varchar(20)        NOT NULL,
    datetime               timestamp          NOT NULL,
    initial_amount         varchar(100)       NOT NULL,
    final_amount           varchar(100)       NOT NULL,
    price                  varchar(100)       NOT NULL,
    order_status           varchar(30)        NOT NULL,
    id_prev_exchange       bigint,
    update_date            timestamp          NOT NULL,
    history_price_block_id bigint,
    price_to_sell          varchar(16),
    stop_price             varchar(20),
    increment_step         integer,
    foreign key (history_price_block_id) references cr_schema.price_history_block (id)
);