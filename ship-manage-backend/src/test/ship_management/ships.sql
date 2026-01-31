create table ship_management.ships
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    name        varchar(100)                          not null comment '船舶名称',
    category_id bigint                                null comment '所属类型ID (外键)',
    tonnage     decimal(10, 2)                        null comment '吨位',
    status      varchar(30) default '在役'            null comment '状态: 在役, 维修中, 停运, 航行中',
    cover_img   varchar(255)                          null comment '船舶照片 (对应图书封面)',
    created_at  datetime    default CURRENT_TIMESTAMP null,
    updated_at  datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '船舶信息表' row_format = DYNAMIC;

create index fk_ships_category
    on ship_management.ships (category_id);

create index idx_name
    on ship_management.ships (name);

create index idx_status
    on ship_management.ships (status);

