-- 创建库
create database if not exists open_api;

-- 切换库
use open_api;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    username     varchar(256)                           null comment '用户昵称',
    account      varchar(256)                           not null comment '账号',
    phone        varchar(256)                           comment '手机号',
    email        varchar(256)                           not null comment '用户邮箱',
    avatar       varchar(1024)                          null comment '用户头像',
    gender       tinyint                                null comment '性别',
    role         varchar(256) default 'user'            not null comment '用户角色：user / admin',
    password     varchar(512)                           not null comment '密码',
    access_key varchar(512) not null comment 'accessKey',
    secret_key varchar(512) not null comment 'secretKey',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除(0-未删, 1-已删)',
    constraint uni_userAccount
    unique (account)
) comment '用户';

-- 接口信息
create table if not exists interface_info
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `name` varchar(256) not null comment '名称',
    `description` varchar(256) null comment '描述',
    `url` varchar(512) not null comment '接口地址',
    `request_params` text null comment '请求参数',
    `request_header` text null comment '请求头',
    `response_header` text null comment '响应头',
    `price` decimal not null comment '计费规则(元/条)',
    `status` int default 0 not null comment '接口状态（0-关闭，1-开启）',
    `method` varchar(256) not null comment '请求类型',
    `creator` bigint not null comment '创建人',
    `create_time` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `update_time` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `is_delete` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
) comment '接口信息';

-- 用户调用接口关系表
create table if not exists `user_interface_info`
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `user_id` bigint not null comment '调用用户 id',
    `interface_info_id` bigint not null comment '接口 id',
    `total_num` int default 0 not null comment '总调用次数',
    `left_num` int default 0 not null comment '剩余调用次数',
    `status` int default 0 not null comment '0-禁用，1-正常',
    `create_time` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `update_time` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `is_delete` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)'
) comment '用户调用接口关系';

-- 创建心灵鸡汤表
CREATE TABLE if not exists `soul_soup` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID' PRIMARY KEY,
   `content` varchar(256) NOT NULL COMMENT '鸡汤内容',
   `create_time` datetime default CURRENT_TIMESTAMP not null COMMENT '创建时间',
   `update_time` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP COMMENT '更新时间',
   `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除'
) COMMENT '心灵鸡汤表';

INSERT INTO soul_soup (content) VALUES ('人生没有过不去的坎，只有过不去的心情。');
INSERT INTO soul_soup (content) VALUES ('不要放弃自己，因为你还有很多机会可以让自己变得更好。');
INSERT INTO soul_soup (content) VALUES ('每一次的努力都是一份收获，每一份收获都是一次成长。');
INSERT INTO soul_soup (content) VALUES ('自己的路自己走，别人的路自己看。');
INSERT INTO soul_soup (content) VALUES ('生命中最重要的是发现自己的价值，而不是别人给你的价值。');
INSERT INTO soul_soup (content) VALUES ('当你感到难过的时候，不要急着去寻找答案，因为答案就在你的内心深处。');
INSERT INTO soul_soup (content) VALUES ('只要你有信心，你就可以创造属于自己的未来。');
INSERT INTO soul_soup (content) VALUES ('人生最大的幸福是做自己喜欢的事情，不必在乎别人的眼光。');
INSERT INTO soul_soup (content) VALUES ('只要你有梦想，你就能创造奇迹。');
INSERT INTO soul_soup (content) VALUES ('人生最大的挑战是战胜自己。');
INSERT INTO soul_soup (content) VALUES ('不要因为没有成功而放弃，因为成功就在不远处。');
INSERT INTO soul_soup (content) VALUES ('当你成功的时候，不要忘记曾经失败的时候。');
INSERT INTO soul_soup (content) VALUES ('只有在经历了失败之后，你才能真正的体会到成功的喜悦。');
INSERT INTO soul_soup (content) VALUES ('成功不是终点，而是一个新的起点。');
INSERT INTO soul_soup (content) VALUES ('成功的关键在于你是否愿意为之奋斗。');
INSERT INTO soul_soup (content) VALUES ('成功的路上充满了艰辛和挫折，但只要坚持到底，你就会成功。');
INSERT INTO soul_soup (content) VALUES ('成功需要勇气和毅力，但更需要耐心和恒心。');
INSERT INTO soul_soup (content) VALUES ('相信自己，你就能创造无限可能。');
INSERT INTO soul_soup (content) VALUES ('只有你不放弃，你才能拥有成功的机会。');
INSERT INTO soul_soup (content) VALUES ('没有人能预测未来，但只有勇敢的人才能创造未来。');
INSERT INTO soul_soup (content) VALUES ('只有你不停止奋斗，你才能走向成功的路上。');
INSERT INTO soul_soup (content) VALUES ('成功不是偶然，而是必然。');
INSERT INTO soul_soup (content) VALUES ('只要你有梦想，你就能创造奇迹。');
INSERT INTO soul_soup (content) VALUES ('只有你不停止奋斗，你才能走向成功的路上。');
