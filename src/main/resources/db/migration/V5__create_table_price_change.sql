CREATE TABLE price_changes
(
    id                 smallserial     NOT NULL,
    old                varchar(100)    NOT NULL,
    new                varchar(100)    NOT NULL,
    state           smallint        NOT NULL,
    have_changes    smallint        NOT NULL,
    PRIMARY KEY (id)
);