create sequence if not exists cr_schema.users_id_seq;

CREATE TABLE cr_schema.users
(
    id       smallint PRIMARY KEY NOT NULL default nextval('cr_schema.users_id_seq'),
    login    varchar(20)          NOT NULL,
    password varchar(100)         NOT NULL
);

INSERT INTO cr_schema.users(login, password)
VALUES ('dyomina', '9770f75f7493dd90e3311a85e84689d7397ad4f6dd42897838784e6d0d637da2');