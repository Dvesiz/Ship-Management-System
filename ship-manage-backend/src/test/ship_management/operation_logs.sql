create table ship_management.operation_logs
(
    id              bigint auto_increment
        primary key,
    user_id         bigint                             null,
    username        varchar(50)                        null,
    module          varchar(50)                        null,
    operation       varchar(50)                        null,
    operation_desc  varchar(500)                       null comment '操作详细描述',
    method          varchar(10)                        null,
    request_url     varchar(255)                       null,
    request_params  text                               null,
    response_result varchar(20)                        null,
    ip_address      varchar(50)                        null,
    user_agent      varchar(500)                       null,
    execution_time  bigint                             null,
    error_msg       text                               null,
    created_at      datetime default CURRENT_TIMESTAMP null
)
    engine = InnoDB
    charset = utf8mb4;

create index idx_created_at
    on ship_management.operation_logs (created_at);

create index idx_module
    on ship_management.operation_logs (module);

create index idx_user_id
    on ship_management.operation_logs (user_id);

