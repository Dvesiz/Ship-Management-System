create table ship_management.maintenance
(
    id               bigint auto_increment comment '主键ID'
        primary key,
    ship_id          bigint                             not null comment '船舶ID',
    description      varchar(255)                       null comment '维修描述',
    cost             decimal(10, 2)                     null comment '维修费用',
    maintenance_time datetime                           null comment '维修时间',
    created_at       datetime default CURRENT_TIMESTAMP null
)
    comment '维修记录表' row_format = DYNAMIC;

create index fk_maintenance_ship
    on ship_management.maintenance (ship_id);

