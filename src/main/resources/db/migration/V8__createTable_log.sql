create sequence if not exists cr_schema.candle_analyze_log_seq_id;
create table cr_schema.candle_analyze_log
(
    id              bigint    not null default nextval('cr_schema.candle_analyze_log_seq_id'),
    first_block_id  bigint,
    middle_block_id bigint,
    last_block_id   bigint,
    type            varchar(10),
    price           varchar(20),
    create_at       timestamp not null,
    primary key (id)
);