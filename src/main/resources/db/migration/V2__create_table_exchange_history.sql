CREATE TABLE exchange_history (
    id               bigint PRIMARY KEY NOT NULL,
    order_id         bigint             NOT NULL,
    type             varchar(20)        NOT NULL,
    datetime         timestamp          NOT NULL,
    initial_amount   varchar(100)       NOT NULL,
    final_amount     varchar(100)       NOT NULL,
    price            varchar(100)       NOT NULL,
    order_status     varchar(30)        NOT NULL,
    id_prev_exchange bigint
);