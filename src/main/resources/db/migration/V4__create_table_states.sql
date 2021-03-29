CREATE TABLE states
(
    id        smallint PRIMARY KEY NOT NULL,
    name      varchar(30)          NOT NULL
);

INSERT INTO states (id, name)
VALUES (4, 'NONE'),
(1, 'ENTER_LOGIN'),
(2, 'ENTER_PASSWORD'),
(3, 'MAIN_MENU');