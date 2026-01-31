create table ship_management.crew
(
    id         bigint auto_increment comment '主键ID'
        primary key,
    name       varchar(50)                        not null comment '船员姓名',
    position   varchar(50)                        null comment '职位 (船长/大副/轮机长)',
    ship_id    bigint                             null comment '当前所属船舶ID',
    phone      varchar(20)                        null comment '联系电话',
    created_at datetime default CURRENT_TIMESTAMP null,
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '船员表' row_format = DYNAMIC;

create index fk_crew_ship
    on ship_management.crew (ship_id);

