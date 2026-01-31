create table ship_management.voyages
(
    id         bigint auto_increment comment '主键ID'
        primary key,
    ship_id    bigint                                not null comment '船舶ID',
    start_port varchar(100)                          null comment '出发港',
    end_port   varchar(100)                          null comment '目的港',
    status     varchar(30) default '计划中'          null comment '航次状态: 计划中, 执行中, 已完成',
    start_time datetime                              null comment '开航时间',
    end_time   datetime                              null comment '结束/抵达时间',
    created_at datetime    default CURRENT_TIMESTAMP null,
    updated_at datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '航次记录表' row_format = DYNAMIC;

create index idx_ship_id
    on ship_management.voyages (ship_id);

