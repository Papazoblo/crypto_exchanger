CREATE TABLE price_history
(
    date         TIMESTAMP   NOT NULL,
    price        VARCHAR(20) NOT NULL,
    change_state VARCHAR(10) NOT NULL,
    PRIMARY KEY (date)
)