alter table cr_schema.price_history_block
    add column time_type varchar(32) default 'MIN_3';

alter table cr_schema.price_history
    drop column history_block_id;