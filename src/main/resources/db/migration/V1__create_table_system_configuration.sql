CREATE TABLE system_configuration
(
    name  varchar(50) PRIMARY KEY NOT NULL,
    value varchar(30)             NOT NULL
);

INSERT INTO system_configuration(name, value)
VALUES ('SYSTEM_STATE', 'LAUNCHED'),
       ('MIN_DIFFERENCE_PRICE', '15'),
       ('CURRENT_PRICE', '3000'),
       ('INVIOLABLE_RESIDUE', '0.005'),
       ('AVAILABLE_MINUTES_COUNT_WITHOUT_EXCHANGE', '45');