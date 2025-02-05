create schema if not exists cr_schema;

set schema 'cr_schema';
set search_path to 'cr_schema';

CREATE TABLE cr_schema.system_configuration
(
    name  varchar(50) PRIMARY KEY NOT NULL,
    value varchar(30)             NOT NULL
);

INSERT INTO cr_schema.system_configuration(name, value)
VALUES ('SYSTEM_STATE', 'LAUNCHED'),
       ('MIN_DIFFERENCE_PRICE', '15'),
       ('CURRENT_PRICE', '3000'),
       ('INVIOLABLE_RESIDUE', '0.005'),
       ('AVAILABLE_MINUTES_COUNT_WITHOUT_EXCHANGE', '45');