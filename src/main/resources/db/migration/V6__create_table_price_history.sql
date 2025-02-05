CREATE SEQUENCE IF NOT EXISTS cr_schema.price_history_block_id_seq;
CREATE TABLE cr_schema.price_history_block
(
    id              BIGINT      NOT NULL default nextval('cr_schema.price_history_block_id_seq'),
    date_open       TIMESTAMP   NOT NULL,
    date_close      TIMESTAMP,
    status          VARCHAR(20) NOT NULL,
    min             VARCHAR(20) DEFAULT '0',
    max             VARCHAR(20) DEFAULT '0',
    avg             VARCHAR(20) DEFAULT '0',
    open            VARCHAR(20) DEFAULT '0',
    close           VARCHAR(20) DEFAULT '0',
    avg_change_type VARCHAR(20),
    PRIMARY KEY (id)
);

CREATE TABLE cr_schema.price_history
(
    date             TIMESTAMP   NOT NULL,
    price            VARCHAR(20) NOT NULL,
    change_state     VARCHAR(10),
    history_block_id BIGINT      NOT NULL,
    PRIMARY KEY (date),
    FOREIGN KEY (history_block_id) REFERENCES price_history_block (id)
);