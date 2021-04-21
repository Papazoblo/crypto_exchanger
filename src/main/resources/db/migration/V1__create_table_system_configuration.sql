CREATE TABLE system_configuration(
    name varchar(50) PRIMARY KEY NOT NULL,
    value varchar(30) NOT NULL
);

INSERT INTO system_configuration(name, value)
VALUES ('MIN_DIFFERENCE_PRICE', '15'),
       ('MIN_DIFFERENCE_PRICE_FIAT_CRYPT', '10'),
       ('MIN_AMOUNT_EXCHANGE', '10'),
       ('MAX_AMOUNT_EXCHANGE', '1000');