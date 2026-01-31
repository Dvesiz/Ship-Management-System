create table ship_management.ship_certificates
(
    id                bigint auto_increment
        primary key,
    ship_id           bigint                                not null,
    certificate_name  varchar(100)                          not null,
    certificate_no    varchar(100)                          null,
    issuing_authority varchar(100)                          null,
    issue_date        date                                  null,
    expiry_date       date                                  null,
    status            varchar(20) default 'VALID'           null,
    attachment_url    varchar(500)                          null,
    remarks           varchar(500)                          null,
    created_at        datetime    default CURRENT_TIMESTAMP null,
    updated_at        datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    engine = InnoDB
    charset = utf8mb4;

create index idx_expiry_date
    on ship_management.ship_certificates (expiry_date);

create index idx_ship_id
    on ship_management.ship_certificates (ship_id);

