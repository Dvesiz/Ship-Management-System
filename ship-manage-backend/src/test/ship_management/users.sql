create table ship_management.users
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    username      varchar(50)                           not null comment '用户名',
    password_hash varchar(255)                          not null comment '加密密码 (对应JWT鉴权)',
    nickname      varchar(50)                           null comment '昵称',
    email         varchar(100)                          null comment '邮箱 (实训要求)',
    avatar_url    varchar(255)                          null comment '头像地址 (实训要求)',
    role          varchar(20) default 'USER'            null comment '角色: USER-普通用户, ADMIN-管理员',
    created_at    datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at    datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_email
        unique (email),
    constraint uk_username
        unique (username)
)
    comment '用户信息表' row_format = DYNAMIC;

