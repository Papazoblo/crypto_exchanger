CREATE TABLE system_configuration(
    name varchar(30) PRIMARY KEY NOT NULL,
    value varchar(30) NOT NULL
);

INSERT INTO system_configuration(name, value)
VALUES ('MIN_DIFFERENCE_PRICE', 15),
VALUES ('MIN_DIFFERENCE_PRICE_FIAT_CRYPT', 10);