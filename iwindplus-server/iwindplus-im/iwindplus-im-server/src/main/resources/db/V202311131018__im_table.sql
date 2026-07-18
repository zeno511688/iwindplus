SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_group
-- ----------------------------
DROP TABLE IF EXISTS `chat_group`;
CREATE TABLE `chat_group`
(
    `id`                  bigint unsigned  NOT NULL COMMENT '主键',
    `created_time`        datetime         NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    `created_timestamp`   bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
    `created_by`          varchar(50)      NOT NULL DEFAULT '' COMMENT '创建人',
    `created_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '创建人主键',
    `modified_time`       datetime         NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
    `modified_timestamp`  bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
    `modified_by`         varchar(50)      NOT NULL DEFAULT '' COMMENT '更新人',
    `modified_id`         bigint unsigned  NOT NULL DEFAULT 0 COMMENT '更新人主键',
    `deleted`             tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
    `version`             int unsigned     NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
    `remark`              varchar(255)     NOT NULL DEFAULT '' COMMENT '备注',
    `status`              int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：封禁，1：正常）',
    `group_name`          varchar(50)      NOT NULL DEFAULT '' COMMENT '群名称',
    `group_avatar`        varchar(255)     NOT NULL DEFAULT '' COMMENT '群头像',
    `group_qrcode`        varchar(255)     NOT NULL DEFAULT '' COMMENT '群二维码',
    `announcement`        varchar(255)     NOT NULL DEFAULT '' COMMENT '公告',
    `limit_num`           int unsigned     NOT NULL DEFAULT 1 COMMENT '群限制数量',
    `show_nick_name_flag` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否显示群成员昵称（0：否，1：是）',
    `edit_avatar_flag`    tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否禁止修改群头像（0：否，1：是）',
    `org_id`              bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_group_name` (`group_name`) COMMENT '普通索引（群名称）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='聊天群表';

-- ----------------------------
-- Table structure for chat_group_user
-- ----------------------------
DROP TABLE IF EXISTS `chat_group_user`;
CREATE TABLE `chat_group_user`
(
    `id`                 bigint unsigned  NOT NULL COMMENT '主键',
    `created_time`       datetime         NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    `created_timestamp`  bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
    `created_by`         varchar(50)      NOT NULL DEFAULT '' COMMENT '创建人',
    `created_id`         bigint unsigned  NOT NULL DEFAULT 0 COMMENT '创建人主键',
    `modified_time`      datetime         NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
    `modified_timestamp` bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
    `modified_by`        varchar(50)      NOT NULL DEFAULT '' COMMENT '更新人',
    `modified_id`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '更新人主键',
    `deleted`            tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
    `version`            int unsigned     NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
    `remark`             varchar(255)     NOT NULL DEFAULT '' COMMENT '备注',
    `nick_name`          varchar(255)     NOT NULL DEFAULT '' COMMENT '群成员昵称',
    `leader_flag`        tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否群主（0：否，1：是）',
    `join_type`          int unsigned     NOT NULL DEFAULT 0 COMMENT '加入类型（0:邀请，1：扫码）',
    `agree_flag`         tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否同意（0：拒绝，1：同意）',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `chat_group_id`      bigint unsigned  NOT NULL DEFAULT 0 COMMENT '聊天群主键',
    `user_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户主键',
    `user_avatar`        varchar(255)     NOT NULL DEFAULT '' COMMENT '用户头像',
    `user_nick_name`     varchar(50)      NOT NULL DEFAULT '' COMMENT '用户昵称',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `uk_chat_group_id_user_id` (`chat_group_id`, `user_id`) COMMENT '复合索引（聊天群主键，用户主键）',
    KEY `idx_chat_group_id` (`chat_group_id`) COMMENT '普通索引（聊天群主键）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）',
    KEY `idx_user_id` (`user_id`) COMMENT '普通索引（用户主键）'
) COMMENT ='聊天群用户表';

-- ----------------------------
-- Table structure for user_friend
-- ----------------------------
DROP TABLE IF EXISTS `user_friend`;
CREATE TABLE `user_friend`
(
    `id`                 bigint unsigned  NOT NULL COMMENT '主键',
    `created_time`       datetime         NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    `created_timestamp`  bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
    `created_by`         varchar(50)      NOT NULL DEFAULT '' COMMENT '创建人',
    `created_id`         bigint unsigned  NOT NULL DEFAULT 0 COMMENT '创建人主键',
    `modified_time`      datetime         NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
    `modified_timestamp` bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
    `modified_by`        varchar(50)      NOT NULL DEFAULT '' COMMENT '更新人',
    `modified_id`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '更新人主键',
    `deleted`            tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
    `version`            int unsigned     NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
    `remark`             varchar(255)     NOT NULL DEFAULT '' COMMENT '备注',
    `status`             tinyint unsigned NOT NULL DEFAULT 0 COMMENT '好友状态（0：待确认，1：已通过，2：已拒绝）',
    `user_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户主键',
    `user_avatar`        varchar(255)     NOT NULL DEFAULT '' COMMENT '用户头像',
    `user_nick_name`     varchar(50)      NOT NULL DEFAULT '' COMMENT '用户昵称',
    `friend_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '好友主键',
    `friend_avatar`      varchar(255)     NOT NULL DEFAULT '' COMMENT '好友头像',
    `friend_nick_name`   varchar(50)      NOT NULL DEFAULT '' COMMENT '好友昵称',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）',
    KEY `idx_user_id` (`user_id`) COMMENT '普通索引（用户主键）',
    KEY `idx_friend_id` (`friend_id`) COMMENT '普通索引（好友主键）'
) COMMENT ='用户好友表';

SET FOREIGN_KEY_CHECKS = 1;
