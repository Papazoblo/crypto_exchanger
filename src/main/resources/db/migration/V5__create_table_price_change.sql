create sequence if not exists cr_schema.price_changes_id_seq;
CREATE TABLE cr_schema.price_changes
(
    id           bigint       NOT NULL default nextval('cr_schema.price_changes_id_seq'),
    old          varchar(100) NOT NULL,
    new          varchar(100) NOT NULL,
    state        smallint     NOT NULL,
    have_changes smallint     NOT NULL,
    PRIMARY KEY (id)
);