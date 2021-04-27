CREATE TABLE states
(
    id        smallserial PRIMARY KEY NOT NULL,
    name      varchar(30)          NOT NULL
);

INSERT INTO states (id, name)
VALUES (1, 'NONE'),
(3, 'AUTHENTICATED');