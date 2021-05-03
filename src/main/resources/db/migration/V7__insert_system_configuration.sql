INSERT INTO system_configuration(name, value)
VALUES ('CURRENT_PRICE_LEVEL', (SELECT price
                                FROM exchange_history
                                ORDER BY id DESC
                                LIMIT 1)),
       ('INVIOLABLE_RESIDUE', '0.005'),
       ('AVAILABLE_MINUTES_COUNT_WITHOUT_EXCHANGE', '45');