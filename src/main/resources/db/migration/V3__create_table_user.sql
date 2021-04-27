CREATE TABLE users
(
    id        smallserial PRIMARY KEY NOT NULL,
    login     varchar(20)          NOT NULL,
    password  varchar(100)         NOT NULL
);

INSERT INTO users(id, login, password)
VALUES (1, 'dyomina', '9770f75f7493dd90e3311a85e84689d7397ad4f6dd42897838784e6d0d637da2');