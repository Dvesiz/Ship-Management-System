create table ship_management.messages
(
    id           bigint auto_increment
        primary key,
    sender_id    bigint      default 0                 null,
    receiver_id  bigint                                not null,
    title        varchar(100)                          null,
    content      text                                  null,
    type         varchar(20) default 'SYSTEM'          null,
    status       varchar(10) default 'UNREAD'          null,
    related_id   bigint                                null,
    related_type varchar(50)                           null,
    created_at   datetime    default CURRENT_TIMESTAMP null,
    read_at      datetime                              null
)
    engine = InnoDB
    charset = utf8mb4;

create index idx_receiver_id
    on ship_management.messages (receiver_id);

create index idx_status
    on ship_management.messages (status);

