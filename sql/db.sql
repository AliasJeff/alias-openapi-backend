-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          bigint                                                        NOT NULL AUTO_INCREMENT COMMENT 'id',
    `username`    varchar(256)                                                           DEFAULT NULL COMMENT '用户昵称',
    `account`     varchar(256)                                                  NOT NULL COMMENT '账号',
    `phone`       varchar(256)                                                           DEFAULT NULL COMMENT '手机号',
    `email`       varchar(255)                                                  NOT NULL,
    `avatar`      varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         DEFAULT 'https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png' COMMENT '用户头像',
    `gender`      tinyint                                                                DEFAULT NULL COMMENT '性别',
    `role`        varchar(256)                                                  NOT NULL DEFAULT 'user' COMMENT '用户角色：user / admin',
    `password`    varchar(512)                                                  NOT NULL COMMENT '密码',
    `access_key`  varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'accessKey',
    `secret_key`  varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'secretKey',
    `create_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint                                                       NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删, 1-已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_userAccount` (`account`)
) COMMENT='用户';

INSERT INTO `user` (`id`, `username`, `account`, `phone`, `email`, `avatar`, `gender`, `role`, `password`, `access_key`, `secret_key`, `create_time`, `update_time`, `is_delete`) VALUES (1378088041316352, NULL, 'admin', NULL, '2022248374@qq.com', 'https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png', NULL, 'admin', '194344925efb6fdd57eb0384376fa278', 'b569bd6b1ebb1c9f193b5739df444b7b', 'c2de412d6a0414b1686dd602195a2396', '2023-04-23 15:52:58', '2023-04-23 15:53:30', 0);


-- ----------------------------
-- Table structure for interface_info
-- ----------------------------
DROP TABLE IF EXISTS `interface_info`;
CREATE TABLE `interface_info`
(
    `id`              bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`            varchar(256)                                                  NOT NULL COMMENT '名称',
    `description`     varchar(256)                                                           DEFAULT NULL COMMENT '描述',
    `method`          varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求类型',
    `url`             varchar(512)                                                  NOT NULL COMMENT '接口地址',
    `request_params`  text COMMENT '请求参数',
    `request_header`  text COMMENT '请求头',
    `response_header` text COMMENT '响应头',
    `price`           decimal(10, 2)                                                NOT NULL COMMENT '计费规则(元/条)\n',
    `status`          int                                                           NOT NULL DEFAULT '0' COMMENT '接口状态（0-关闭，1-开启）',
    `creator`         bigint                                                        NOT NULL COMMENT '创建人',
    `create_time`     datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`       tinyint                                                       NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删, 1-已删)',
    PRIMARY KEY (`id`)
) COMMENT='接口信息';

-- ----------------------------
-- Table structure for user_interface_info
-- ----------------------------
DROP TABLE IF EXISTS `user_interface_info`;
CREATE TABLE `user_interface_info`
(
    `id`                bigint   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`           bigint   NOT NULL COMMENT '调用用户 id',
    `interface_info_id` bigint   NOT NULL COMMENT '接口 id',
    `total_num`         int      NOT NULL DEFAULT '0' COMMENT '总调用次数',
    `left_num`          int      NOT NULL DEFAULT '0' COMMENT '剩余调用次数',
    `status`            int      NOT NULL DEFAULT '1' COMMENT '0-禁用，1-正常',
    `create_time`       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`         tinyint  NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删, 1-已删)',
    PRIMARY KEY (`id`)
) COMMENT='用户调用接口关系';

-- ----------------------------
-- Table structure for soul_soup
-- ----------------------------
DROP TABLE IF EXISTS `soul_soup`;
CREATE TABLE `soul_soup`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content`     varchar(256) NOT NULL COMMENT '鸡汤内容',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
    PRIMARY KEY (`id`)
) COMMENT='心灵鸡汤表';
