SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for mail_tpl
-- ----------------------------
DROP TABLE IF EXISTS `mail_tpl`;
CREATE TABLE `mail_tpl` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `created_timestamp` bigint(20) unsigned NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
  `created_by` varchar(50) NOT NULL DEFAULT '' COMMENT '创建人',
  `created_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '创建人主键',
  `modified_timestamp` bigint(20) unsigned NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
  `modified_by` varchar(50) NOT NULL DEFAULT '' COMMENT '更新人',
  `modified_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '更新人主键',
  `deleted` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
  `version` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
  `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `status` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
  `code` varchar(50) NOT NULL DEFAULT '' COMMENT '编码',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `template_content` varchar(5000) NOT NULL DEFAULT '' COMMENT '模板内容',
  `captcha_timeout` int(10) unsigned NOT NULL DEFAULT 10 COMMENT '验证码有效时间（单位：分钟）',
  `captcha_length` int(10) unsigned NOT NULL DEFAULT 6 COMMENT '验证码长度',
  `limit_count_day` int(10) unsigned NOT NULL DEFAULT 10 COMMENT '限制每天次数',
  `limit_count_hour` int(10) unsigned NOT NULL DEFAULT 5 COMMENT '限制每小时次数',
  `limit_count_minute` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '限制每分钟次数',
  `build_in_flag` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
  `org_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '组织主键',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
  KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT='邮箱模板表';

-- ----------------------------
-- Table structure for oss_tpl
-- ----------------------------
DROP TABLE IF EXISTS `oss_tpl`;
CREATE TABLE `oss_tpl` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `created_timestamp` bigint(20) unsigned NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
  `created_by` varchar(50) NOT NULL DEFAULT '' COMMENT '创建人',
  `created_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '创建人主键',
  `modified_timestamp` bigint(20) unsigned NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
  `modified_by` varchar(50) NOT NULL DEFAULT '' COMMENT '更新人',
  `modified_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '更新人主键',
  `deleted` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
  `version` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
  `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `status` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
  `type` tinyint(2) NOT NULL DEFAULT 0 COMMENT '对象存储类型（0：minio，1：阿里云，2：七牛云）',
  `code` varchar(50) NOT NULL DEFAULT '' COMMENT '编码',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `bucket_name` varchar(100) NOT NULL DEFAULT '' COMMENT '空间名',
  `access_domain` varchar(100) NOT NULL DEFAULT '' COMMENT '自定义访问域名',
  `build_in_flag` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
  `org_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '组织主键',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
  KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT='对象存储模板表';

-- ----------------------------
-- Table structure for sms_tpl
-- ----------------------------
DROP TABLE IF EXISTS `sms_tpl`;
CREATE TABLE `sms_tpl` (
  `id` bigint(20) unsigned NOT NULL COMMENT '主键',
  `created_timestamp` bigint(20) unsigned NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
  `created_by` varchar(50) NOT NULL DEFAULT '' COMMENT '创建人',
  `created_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '创建人主键',
  `modified_timestamp` bigint(20) unsigned NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
  `modified_by` varchar(50) NOT NULL DEFAULT '' COMMENT '更新人',
  `modified_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '更新人主键',
  `deleted` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
  `version` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
  `remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `status` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
  `type` tinyint(2) NOT NULL DEFAULT 0 COMMENT '短信类型（0：阿里云，1：七牛云，2：麦讯通，3：凌凯）',
  `code` varchar(50) NOT NULL DEFAULT '' COMMENT '编码',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '名称',
  `sign_name` varchar(100) NOT NULL DEFAULT '' COMMENT '短信签名',
  `template_content` varchar(1000) NOT NULL DEFAULT '' COMMENT '短信模板内容',
  `captcha_timeout` int(10) unsigned NOT NULL DEFAULT 10 COMMENT '验证码有效时间（单位：分钟）',
  `captcha_length` int(10) unsigned NOT NULL DEFAULT 6 COMMENT '验证码长度',
  `limit_count_day` int(10) unsigned NOT NULL DEFAULT 10 COMMENT '限制每天次数',
  `limit_count_hour` int(10) unsigned NOT NULL DEFAULT 5 COMMENT '限制每小时次数',
  `limit_count_minute` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '限制每分钟次数',
  `build_in_flag` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
  `org_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '组织主键',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
  KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT='短信模板表';

SET FOREIGN_KEY_CHECKS = 1;
