create table ship_management.ship_categories
(
    id         bigint auto_increment comment '主键ID'
        primary key,
    name       varchar(50)                        not null comment '类型名称 (如: 散货船)',
    alias      varchar(50)                        null comment '类型别名/编码',
    created_at datetime default CURRENT_TIMESTAMP null,
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_name
        unique (name)
)
    comment '船舶类型表' row_format = DYNAMIC;

