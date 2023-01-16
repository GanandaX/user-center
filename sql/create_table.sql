-- auto-generated definition
create table user
(
    id           bigint auto_increment comment '用户唯一标识'
        primary key,
    userAccount  varchar(256)                       null comment '用户 登录账号',
    userPassword varchar(512)                       not null comment '用户登录密码',
    username     varchar(512)                       null comment '用户昵称',
    avatarUrl    varchar(1024)                      null comment '用户图像地址',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '电子邮件',
    userStatus   int      default 0                 not null comment '用户状态 0-正常',
    gender       tinyint                            null comment '性别 0-男 1-女',
    userRole     int      default 0                 not null comment '用户角色 0-普通用户  1-管理员',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除 0-正常 1-刪除（逻辑删除）'
)
    comment '用户表';




