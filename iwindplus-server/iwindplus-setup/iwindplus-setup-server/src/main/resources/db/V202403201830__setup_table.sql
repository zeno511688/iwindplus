SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for mail_config
-- ----------------------------
DROP TABLE IF EXISTS `mail_config`;
CREATE TABLE `mail_config`
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
    `status`             int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `nick_name`          varchar(255)     NOT NULL DEFAULT '' COMMENT '发件人昵称',
    `host`               varchar(255)     NOT NULL DEFAULT '' COMMENT '发件服务器域名',
    `username`           varchar(255)     NOT NULL DEFAULT '' COMMENT '发件服务器账户',
    `password`           varchar(255)     NOT NULL DEFAULT '' COMMENT '发件服务器密码',
    `port`               int unsigned     NOT NULL DEFAULT 465 COMMENT '发件服务器端口',
    `ssl_enable`         tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否启用ssl（0：否，1：是）',
    `retry_enable`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否启用重试（0：否，1：是）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否启用重试（0：否，1：是）',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='邮箱配置表';

-- ----------------------------
-- Table structure for mail_tpl
-- ----------------------------
DROP TABLE IF EXISTS `mail_tpl`;
CREATE TABLE `mail_tpl`
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
    `status`             int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `template_content`   varchar(5000)    NOT NULL DEFAULT '' COMMENT '模板内容',
    `captcha_timeout`    int unsigned     NOT NULL DEFAULT 10 COMMENT '验证码有效时间（单位：分钟）',
    `captcha_length`     int unsigned     NOT NULL DEFAULT 6 COMMENT '验证码长度',
    `limit_count_day`    int unsigned     NOT NULL DEFAULT 10 COMMENT '限制每天次数',
    `limit_count_hour`   int unsigned     NOT NULL DEFAULT 5 COMMENT '限制每小时次数',
    `limit_count_minute` int unsigned     NOT NULL DEFAULT 1 COMMENT '限制每分钟次数',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `config_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '配置主键',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_config_id` (`config_id`) COMMENT '普通索引（配置主键）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='邮箱模板表';

-- ----------------------------
-- Table structure for oss_config
-- ----------------------------
DROP TABLE IF EXISTS `oss_config`;
CREATE TABLE `oss_config`
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
    `status`             int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `type`               int unsigned     NOT NULL DEFAULT 0 COMMENT '类型（0：minio，1：阿里云，2：七牛云）',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `oss_endpoint`       varchar(100)     NOT NULL DEFAULT '' COMMENT 'oss地域节点',
    `access_key`         varchar(100)     NOT NULL DEFAULT '' COMMENT '访问key',
    `secret_key`         varchar(255)     NOT NULL DEFAULT '' COMMENT '密钥',
    `sts_endpoint`       varchar(255)     NOT NULL DEFAULT '' COMMENT 'sts地域节点',
    `role_arn`           varchar(255)     NOT NULL DEFAULT '' COMMENT 'RAM角色',
    `policy`             varchar(1024)    NOT NULL DEFAULT '' COMMENT 'RAM权限策略（如果policy为空，则用户将获得该角色下所有权限）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='对象存储配置表';

-- ----------------------------
-- Table structure for oss_tpl
-- ----------------------------
DROP TABLE IF EXISTS `oss_tpl`;
CREATE TABLE `oss_tpl`
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
    `status`             int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `bucket_name`        varchar(100)     NOT NULL DEFAULT '' COMMENT '空间名',
    `access_domain`      varchar(100)     NOT NULL DEFAULT '' COMMENT '自定义访问域名',
    `part_size`          int unsigned     NOT NULL DEFAULT 50 COMMENT '分片上传，分片大小（单位：兆）',
    `broke`              tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否启用断点上传（0：否，1：是）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `config_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '配置主键',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_config_id` (`config_id`) COMMENT '普通索引（配置主键）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='对象存储模板表';

-- ----------------------------
-- Table structure for sms_config
-- ----------------------------
DROP TABLE IF EXISTS `sms_config`;
CREATE TABLE `sms_config`
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
    `status`             int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `type`               int unsigned     NOT NULL DEFAULT 0 COMMENT '类型（0：阿里云，1：七牛云，2：麦讯通，3：凌凯）',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `access_key`         varchar(100)     NOT NULL DEFAULT '' COMMENT '访问key',
    `secret_key`         varchar(255)     NOT NULL DEFAULT '' COMMENT '密钥',
    `sts_endpoint`       varchar(255)     NOT NULL DEFAULT '' COMMENT 'sts地域节点',
    `role_arn`           varchar(255)     NOT NULL DEFAULT '' COMMENT 'RAM角色',
    `policy`             tinytext         NOT NULL DEFAULT '' COMMENT 'RAM权限策略（如果policy为空，则用户将获得该角色下所有权限）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='阿里云短信配置表';

-- ----------------------------
-- Table structure for sms_tpl
-- ----------------------------
DROP TABLE IF EXISTS `sms_tpl`;
CREATE TABLE `sms_tpl`
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
    `status`             int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(255)     NOT NULL DEFAULT '' COMMENT '名称',
    `sign_name`          varchar(100)     NOT NULL DEFAULT '' COMMENT '短信签名',
    `template_content`   varchar(1000)    NOT NULL DEFAULT '' COMMENT '短信模板内容',
    `captcha_timeout`    int unsigned     NOT NULL DEFAULT 10 COMMENT '验证码有效时间（单位：分钟）',
    `captcha_length`     int unsigned     NOT NULL DEFAULT 6 COMMENT '验证码长度',
    `limit_count_day`    int unsigned     NOT NULL DEFAULT 10 COMMENT '限制每天次数',
    `limit_count_hour`   int unsigned     NOT NULL DEFAULT 5 COMMENT '限制每小时次数',
    `limit_count_minute` int unsigned     NOT NULL DEFAULT 1 COMMENT '限制每分钟次数',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `config_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '配置主键',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_config_id` (`config_id`) COMMENT '普通索引（配置主键）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='阿里云短信模板表';

-- ----------------------------
-- Table structure for vod_config
-- ----------------------------
DROP TABLE IF EXISTS `vod_config`;
CREATE TABLE `vod_config`
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
    `status`             int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `type`               int unsigned     NOT NULL DEFAULT 0 COMMENT '类型（0：阿里云，1：七牛云）',
    `code`               varchar(255)     NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `region`             varchar(100)     NOT NULL DEFAULT '' COMMENT '服务器区域',
    `access_key`         varchar(100)     NOT NULL DEFAULT '' COMMENT '访问key',
    `secret_key`         varchar(255)     NOT NULL DEFAULT '' COMMENT '密钥',
    `sts_endpoint`       varchar(255)     NOT NULL DEFAULT '' COMMENT 'sts地域节点',
    `role_arn`           varchar(255)     NOT NULL DEFAULT '' COMMENT 'RAM角色',
    `policy`             varchar(1024)    NOT NULL DEFAULT '' COMMENT 'RAM权限策略（如果policy为空，则用户将获得该角色下所有权限）\n',
    `notify_url`         varchar(255)     NOT NULL DEFAULT '' COMMENT '回调地址',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='阿里云视频点播配置表';

-- ----------------------------
-- Table structure for wechat_config_ma
-- ----------------------------
DROP TABLE IF EXISTS `wechat_config_ma`;
CREATE TABLE `wechat_config_ma`
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
    `status`             int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `code`               varchar(255)     NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `access_key`         varchar(100)     NOT NULL DEFAULT '' COMMENT '小程序的appId',
    `secret_key`         varchar(255)     NOT NULL DEFAULT '' COMMENT '小程序密钥',
    `token`              varchar(100)     NOT NULL DEFAULT '' COMMENT '消息推送token',
    `aes_key`            varchar(100)     NOT NULL DEFAULT '' COMMENT '消息推送加密密钥',
    `msg_data_format`    varchar(100)     NOT NULL DEFAULT 'JSON' COMMENT '消息推送数据格式，XML或者JSON',
    `use_redis`          tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否使用redis存储',
    `qrcode`             varchar(255)     NOT NULL DEFAULT '' COMMENT '小程序二维码',
    `notify_success_url` varchar(255)     NOT NULL DEFAULT '' COMMENT '回调成功地址（外网，用户授权登录方式用）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='微信小程序配置表';

-- ----------------------------
-- Table structure for wechat_config_mp
-- ----------------------------
DROP TABLE IF EXISTS `wechat_config_mp`;
CREATE TABLE `wechat_config_mp`
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
    `status`             int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `code`               varchar(255)     NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `access_key`         varchar(100)     NOT NULL DEFAULT '' COMMENT '公众号的appId',
    `secret_key`         varchar(255)     NOT NULL DEFAULT '' COMMENT '公众号密钥',
    `token`              varchar(100)     NOT NULL DEFAULT '' COMMENT '消息推送token',
    `aes_key`            varchar(100)     NOT NULL DEFAULT '' COMMENT '消息推送加密密钥',
    `use_redis`          tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否使用redis存储',
    `notify_url`         varchar(255)     NOT NULL DEFAULT '' COMMENT '回调地址（外网，扫码登陆用）',
    `notify_success_url` varchar(255)     NOT NULL DEFAULT '' COMMENT '回调成功地址（扫码登陆用）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='微信公众号配置表';

SET FOREIGN_KEY_CHECKS = 1;
