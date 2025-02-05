create sequence if not exists cr_schema.exchange_history_id_seq;
CREATE TABLE cr_schema.exchange_history
(
    id                 bigint PRIMARY KEY NOT NULL default nextval('cr_schema.exchange_history_id_seq'),
    order_id           bigint             NOT NULL,
    type               varchar(20)        NOT NULL,
    datetime           timestamp          NOT NULL,
    initial_amount     varchar(100)       NOT NULL,
    final_amount       varchar(100)       NOT NULL,
    price              varchar(100)       NOT NULL,
    order_status       varchar(30)        NOT NULL,
    id_prev_exchange   bigint,
    update_date        timestamp          NOT NULL,
    cancel_type        varchar(50),
    min_price_exchange varchar(16),
    increment_step     integer
);