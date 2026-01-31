create table ship_management.fuel_records
(
    id          bigint auto_increment
        primary key,
    ship_id     bigint                                not null,
    voyage_id   bigint                                null,
    record_date date                                  not null,
    fuel_type   varchar(20) default 'HFO'             null,
    quantity    decimal(10, 2)                        null,
    unit_price  decimal(10, 2)                        null,
    total_cost  decimal(12, 2)                        null,
    supplier    varchar(100)                          null,
    port        varchar(100)                          null,
    remarks     varchar(500)                          null,
    created_at  datetime    default CURRENT_TIMESTAMP null,
    updated_at  datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    engine = InnoDB
    charset = utf8mb4;

create index idx_record_date
    on ship_management.fuel_records (record_date);

create index idx_ship_id
    on ship_management.fuel_records (ship_id);

