SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_cert
-- ----------------------------
DROP TABLE IF EXISTS `app_cert`;
CREATE TABLE `app_cert`
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
    `cert_type`          int unsigned     NOT NULL DEFAULT 0 COMMENT '凭证类型（0：API签名）',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `access_key`         varchar(100)     NOT NULL DEFAULT '' COMMENT '访问key',
    `secret_key`         varchar(255)     NOT NULL DEFAULT '' COMMENT '密钥',
    `timeout`            int unsigned     NOT NULL DEFAULT 30 COMMENT '超时时间（单位：秒）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_access_key` (`access_key`) COMMENT '普通索引（访问key）'
) COMMENT ='应用凭证表';

-- ----------------------------
-- Table structure for client
-- ----------------------------
DROP TABLE IF EXISTS `client`;
CREATE TABLE `client`
(
    `id`                       bigint unsigned  NOT NULL COMMENT '主键',
    `created_time`             datetime         NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    `created_timestamp`        bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
    `created_by`               varchar(50)      NOT NULL DEFAULT '' COMMENT '创建人',
    `created_id`               bigint unsigned  NOT NULL DEFAULT 0 COMMENT '创建人主键',
    `modified_time`            datetime         NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT '更新时间',
    `modified_timestamp`       bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
    `modified_by`              varchar(50)      NOT NULL DEFAULT '' COMMENT '更新人',
    `modified_id`              bigint unsigned  NOT NULL DEFAULT 0 COMMENT '更新人主键',
    `deleted`                  tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
    `version`                  int unsigned     NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
    `remark`                   varchar(255)     NOT NULL DEFAULT '' COMMENT '备注',
    `status`                   int unsigned     NOT NULL DEFAULT 1 COMMENT '状态（0：禁用，1：启用，2：锁定）',
    `client_id`                varchar(50)      NOT NULL DEFAULT '' COMMENT '客户端id',
    `client_name`              varchar(50)      NOT NULL DEFAULT '' COMMENT '客户端名称',
    `client_id_issued_at`      datetime         NOT NULL DEFAULT current_timestamp() COMMENT '客户端签发时间',
    `client_secret`            varchar(255)     NOT NULL DEFAULT '' COMMENT '客户端密钥',
    `client_secret_expires_at` datetime         NOT NULL COMMENT '客户端密钥过期时间',
    `authentication_method`    varchar(1000)    NOT NULL DEFAULT '' COMMENT '认证方法',
    `authorized_grant_type`    varchar(1000)    NOT NULL DEFAULT '' COMMENT '客户端支持的grant_type',
    `redirect_uri`             varchar(1000)    NOT NULL DEFAULT '' COMMENT '重定向URI',
    `logout_redirect_uri`      varchar(1000)    NOT NULL DEFAULT '' COMMENT '退出重定向地址',
    `scope`                    varchar(255)     NOT NULL DEFAULT '' COMMENT '客户端申请的权限范围',
    `client_setting`           varchar(2000)    NOT NULL DEFAULT '' COMMENT '访问token有效时间',
    `token_setting`            varchar(2000)    NOT NULL DEFAULT '' COMMENT '刷新token有效时间',
    PRIMARY KEY (`id`),
    KEY `idx_client_id` (`client_id`) COMMENT '普通索引（客户端id）',
    KEY `idx_client_name` (`client_name`) COMMENT '普通索引（客户端名称）'
) COMMENT ='客户端表';

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`
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
    `name`               varchar(100)     NOT NULL DEFAULT '' COMMENT '名称',
    `level`              int unsigned     NOT NULL DEFAULT 1 COMMENT '级别',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `parent_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '父类主键',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code_org_id_parent_id` (`code`, `org_id`, `parent_id`) COMMENT '复合索引（编码，组织主键，父类主键）',
    KEY `idx_name_org_id_parent_id` (`name`, `org_id`, `parent_id`) COMMENT '复合索引（名称，组织主键，父类主键）',
    KEY `idx_org_id_parent_id` (`org_id`, `parent_id`) COMMENT '复合索引（组织主键，父类主键）'
) COMMENT ='部门表';

-- ----------------------------
-- Table structure for i18n_project
-- ----------------------------
DROP TABLE IF EXISTS `i18n_project`;
CREATE TABLE `i18n_project`
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
    `platform_type`      int unsigned     NOT NULL DEFAULT 0 COMMENT '平台类型（0：mgt，1：web）',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `file_name`          varchar(50)      NOT NULL DEFAULT '' COMMENT '文件名',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_file_name` (`file_name`) COMMENT '普通索引（文件名）'
) COMMENT ='国际化项目表';

-- ----------------------------
-- Table structure for i18n_msg
-- ----------------------------
DROP TABLE IF EXISTS `i18n_msg`;
CREATE TABLE `i18n_msg`
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
    `code`               varchar(100)     NOT NULL DEFAULT '' COMMENT '编码',
    `value`              varchar(200)     NOT NULL DEFAULT '' COMMENT '值',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `project_id`         bigint unsigned  NOT NULL DEFAULT 0 COMMENT '项目主键',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`) COMMENT '普通索引（项目主键）',
    KEY `idx_name` (`code`) COMMENT '普通索引（编码）'
) COMMENT ='国际化消息表';

-- ----------------------------
-- Table structure for system
-- ----------------------------
DROP TABLE IF EXISTS `system`;
CREATE TABLE `system`
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
    `icon_style`         varchar(255)     NOT NULL DEFAULT '' COMMENT '图标样式',
    `icon_url`           varchar(255)     NOT NULL DEFAULT '' COMMENT '图标路径',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `hide_flag`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否隐藏（0：否，1：是）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）'
) COMMENT ='系统表';
-- ----------------------------
-- Table structure for org
-- ----------------------------
DROP TABLE IF EXISTS `org`;
CREATE TABLE `org`
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
    `audit_status`       int unsigned     NOT NULL DEFAULT 0 COMMENT '审核状态（0：新建，1：待审核，2：已审核，3：已驳回）',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `abbr`               varchar(50)      NOT NULL DEFAULT '' COMMENT '简称',
    `uscc`               varchar(255)     NOT NULL DEFAULT '' COMMENT '社会信用代码',
    `legal_person`       varchar(50)      NOT NULL DEFAULT '' COMMENT '企业法人',
    `leader`             varchar(50)      NOT NULL DEFAULT '' COMMENT '负责人',
    `manager`            varchar(50)      NOT NULL DEFAULT '' COMMENT '分管领导',
    `logo`               varchar(255)     NOT NULL DEFAULT '' COMMENT 'logo',
    `business_license`   varchar(255)     NOT NULL DEFAULT '' COMMENT '营业执照',
    `mail`               varchar(50)      NOT NULL DEFAULT '' COMMENT '邮箱',
    `mobile`             varchar(50)      NOT NULL DEFAULT '' COMMENT '手机',
    `telephone`          varchar(50)      NOT NULL DEFAULT '' COMMENT '座机',
    `country`            varchar(50)      NOT NULL DEFAULT '中国' COMMENT '国家',
    `province`           varchar(50)      NOT NULL DEFAULT '' COMMENT '省份',
    `city`               varchar(50)      NOT NULL DEFAULT '' COMMENT '城市',
    `district`           varchar(50)      NOT NULL DEFAULT '' COMMENT '地区',
    `detail_address`     varchar(255)     NOT NULL DEFAULT '' COMMENT '街道/详细地址',
    `website`            varchar(255)     NOT NULL DEFAULT '' COMMENT '官网地址',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）'
) COMMENT ='组织表';

-- ----------------------------
-- Table structure for org_audit
-- ----------------------------
DROP TABLE IF EXISTS `org_audit`;
CREATE TABLE `org_audit`
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
    `remark`             varchar(255)     NOT NULL DEFAULT '' COMMENT '审核意见',
    `audit_status`       int unsigned     NOT NULL DEFAULT 0 COMMENT '审核状态（0：新建，1：待审核，2：已审核，3：已驳回）',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='组织审核表';

-- ----------------------------
-- Table structure for org_extend
-- ----------------------------
DROP TABLE IF EXISTS `org_extend`;
CREATE TABLE `org_extend`
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
    `intro`              varchar(5000)    NOT NULL DEFAULT '' COMMENT '简介',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='组织扩展表';

-- ----------------------------
-- Table structure for position
-- ----------------------------
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position`
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
    `name`               varchar(100)     NOT NULL DEFAULT '' COMMENT '名称',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `department_id`      bigint unsigned  NOT NULL DEFAULT 0 COMMENT '部门主键',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code_department_id_org_id` (`code`, `department_id`, `org_id`) COMMENT '复合索引（编码，部门主键，组织主键）',
    KEY `idx_name_department_id_org_id` (`name`, `department_id`, `org_id`) COMMENT '复合索引（名称，部门主键，组织主键）',
    KEY `idx_department_id_org_id` (`department_id`, `org_id`) COMMENT '复合索引（部门主键，组织主键）'
) COMMENT ='职位表';

-- ----------------------------
-- Table structure for menu
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu`
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
    `icon_url`           varchar(255)     NOT NULL DEFAULT '' COMMENT '图标路径',
    `icon_style`         varchar(255)     NOT NULL DEFAULT '' COMMENT '图标样式',
    `route_url`          varchar(255)     NOT NULL DEFAULT '' COMMENT '路由路径',
    `level`              int unsigned     NOT NULL DEFAULT 1 COMMENT '级别',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `hide_flag`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否隐藏（0：否，1：是）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `parent_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '父类主键',
    `system_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '系统主键',
    PRIMARY KEY (`id`),
    KEY `idx_code_system_id` (`code`, `system_id`) COMMENT '复合索引（编码，系统主键）',
    KEY `idx_name_system_id_parent_id` (`name`, `system_id`, `parent_id`) COMMENT '复合索引（名称，父类主键，系统主键）',
    KEY `idx_system_id` (`system_id`) COMMENT '普通索引（系统主键）'
) COMMENT ='菜单表';

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`
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
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `default_flag`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否默认（0：否，1：是）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code_org_id` (`code`, `org_id`) COMMENT '复合索引（编码，组织主键）',
    KEY `idx_name_org_id` (`name`, `org_id`) COMMENT '复合索引（名称，组织主键）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='角色表';

-- ----------------------------
-- Table structure for resource
-- ----------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource`
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
    `resource_type`      int unsigned     NOT NULL DEFAULT 1 COMMENT '资源类型（1：按钮，2：API）',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `name`               varchar(50)      NOT NULL DEFAULT '' COMMENT '名称',
    `request_method`     varchar(10)      NOT NULL DEFAULT '' COMMENT '请求方式',
    `api_url`            varchar(255)     NOT NULL DEFAULT '' COMMENT 'API路径',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `menu_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '菜单主键',
    PRIMARY KEY (`id`),
    KEY `idx_menu_id_name` (`menu_id`, `name`) COMMENT '复合索引（菜单主键，名称）',
    KEY `idx_request_method_api_url` (`request_method`, `api_url`) COMMENT '普通索引（请求方式，API路径）'
) COMMENT ='资源表';
-- ----------------------------
-- Table structure for role_menu
-- ----------------------------
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu`
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
    `role_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '角色主键',
    `menu_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '菜单主键',
    PRIMARY KEY (`id`),
    KEY `idx_role_id` (`role_id`) COMMENT '普通索引（角色主键）',
    KEY `idx_menu_id` (`menu_id`) COMMENT '普通索引（菜单主键）'
) COMMENT ='角色菜单关系表';

-- ----------------------------
-- Table structure for role_resource
-- ----------------------------
DROP TABLE IF EXISTS `role_resource`;
CREATE TABLE `role_resource`
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
    `role_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '角色主键',
    `menu_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '菜单主键',
    `resource_id`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '资源主键',
    PRIMARY KEY (`id`),
    KEY `idx_role_id` (`role_id`) COMMENT '普通索引（角色主键）',
    KEY `idx_menu_id` (`menu_id`) COMMENT '普通索引（菜单主键）',
    KEY `idx_resource_id` (`resource_id`) COMMENT '普通索引（资源主键）'
) COMMENT ='角色资源关系表';

-- ----------------------------
-- Table structure for server
-- ----------------------------
DROP TABLE IF EXISTS `server`;
CREATE TABLE `server`
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
    `name`               varchar(100)     NOT NULL DEFAULT '' COMMENT '名称',
    `route_id`           varchar(100)     NOT NULL DEFAULT '' COMMENT '路由ID',
    `uri`                varchar(100)     NOT NULL DEFAULT '' COMMENT '服务地址',
    `predicates`         varchar(1000)    NOT NULL DEFAULT '' COMMENT '路由规则',
    `filters`            varchar(1000)    NOT NULL DEFAULT '' COMMENT '路由过滤规则',
    `metadata`           varchar(1000)    NOT NULL DEFAULT '' COMMENT '元数据',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序',
    `hide_flag`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否隐藏（0：否，1：是）',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_route_id` (`route_id`) COMMENT '普通索引（服务名称）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）'
) COMMENT ='服务表';

-- ----------------------------
-- Table structure for server_api
-- ----------------------------
DROP TABLE IF EXISTS `server_api`;
CREATE TABLE `server_api`
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
    `app_name`           varchar(100)     NOT NULL DEFAULT '' COMMENT '应用名称',
    `controller_name`    varchar(100)     NOT NULL DEFAULT '' COMMENT '控制器名称',
    `request_method`     varchar(10)      NOT NULL DEFAULT '' COMMENT '请求方式',
    `api_name`           varchar(100)     NOT NULL DEFAULT '' COMMENT 'API名称',
    `api_url`            varchar(255)     NOT NULL DEFAULT '' COMMENT 'API路径',
    `rate`               bigint unsigned  NOT NULL DEFAULT 2000 COMMENT '限流次数，每个时间窗口允许请求数量',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `hide_flag`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否隐藏（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_app_name_controller_name` (`app_name`, `controller_name`) COMMENT '复合索引（应用名称，控制器名称）',
    KEY `idx_app_name_api_name` (`app_name`, `api_name`) COMMENT '复合索引（应用名称，API名称）',
    KEY `idx_api_url` (`api_url`(100)) COMMENT '普通索引（API路径）'
) COMMENT ='服务API表';

-- ----------------------------
-- Table structure for api_white_list
-- ----------------------------
DROP TABLE IF EXISTS `api_white_list`;
CREATE TABLE `api_white_list`
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
    `name`               varchar(100)     NOT NULL DEFAULT '' COMMENT '名称',
    `code`               varchar(50)      NOT NULL DEFAULT '' COMMENT '编码',
    `api_url`            varchar(255)     NOT NULL DEFAULT '' COMMENT 'API路径',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_api_url` (`api_url`) COMMENT '普通索引（API路径）'
) COMMENT ='API白名单表';

-- ----------------------------
-- Table structure for ip_black_list
-- ----------------------------
DROP TABLE IF EXISTS `ip_black_list`;
CREATE TABLE `ip_black_list`
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
    `ip`                 varchar(100)     NOT NULL DEFAULT '' COMMENT 'IP',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_ip` (`ip`) COMMENT '普通索引（IP）'
) COMMENT ='IP黑名单表';


-- ----------------------------
-- Table structure for third_bind_grant
-- ----------------------------
DROP TABLE IF EXISTS `third_bind_grant`;
CREATE TABLE `third_bind_grant`
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
    `code`               varchar(255)     NOT NULL DEFAULT '' COMMENT '编码',
    `openid`             varchar(255)     NOT NULL DEFAULT '' COMMENT '用户唯一标识',
    `union_id`           varchar(255)     NOT NULL DEFAULT '' COMMENT '用户在开放平台的唯一标识符',
    `type`               tinyint unsigned NOT NULL DEFAULT 0 COMMENT '类型（0：微信公众号，1：微信小程序，2：企业微信）',
    `user_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户主键',
    PRIMARY KEY (`id`),
    KEY `idx_openid` (`openid`) COMMENT '普通索引（用户唯一标识）',
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_user_id` (`user_id`) COMMENT '普通索引（用户主键）'
) COMMENT ='第三方绑定授权表';

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
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
    `enabled`             tinyint unsigned NOT NULL DEFAULT 1 COMMENT '账号是否启用（0：禁用，1：启用）',
    `locked`              tinyint unsigned NOT NULL DEFAULT 0 COMMENT '账号是否锁定（0：未锁定，1：已锁定）',
    `account_expired`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '账号是否过期（0：未过期，1：已过期）',
    `credentials_expired` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '密码是否过期（0：未过期，1：已过期）',
    `job_number`          varchar(50)      NOT NULL DEFAULT '' COMMENT '工号',
    `username`            varchar(50)      NOT NULL DEFAULT '' COMMENT '用户名',
    `mobile`              varchar(50)      NOT NULL DEFAULT '' COMMENT '手机',
    `real_name`           varchar(50)      NOT NULL DEFAULT '' COMMENT '姓名',
    `password`            varchar(255)     NOT NULL DEFAULT '' COMMENT '密码',
    `sex`                 int unsigned     NOT NULL DEFAULT 0 COMMENT '性别（0：未知，1：男，2：女 ）',
    `mail`                varchar(50)      NOT NULL DEFAULT '' COMMENT '邮箱',
    `nick_name`           varchar(50)      NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar`              varchar(255)     NOT NULL DEFAULT '' COMMENT '头像',
    `birthday`            varchar(255)     NOT NULL DEFAULT '' COMMENT '生日',
    `id_card`             varchar(50)      NOT NULL DEFAULT '' COMMENT '身份证',
    `id_card_front`       varchar(255)     NOT NULL DEFAULT '' COMMENT '身份证正面',
    `id_card_back`        varchar(255)     NOT NULL DEFAULT '' COMMENT '身份证背面',
    `country`             varchar(50)      NOT NULL DEFAULT '中国' COMMENT '国家（身份证）',
    `province`            varchar(50)      NOT NULL DEFAULT '' COMMENT '省份（身份证）',
    `city`                varchar(50)      NOT NULL DEFAULT '' COMMENT '城市（身份证）',
    `district`            varchar(50)      NOT NULL DEFAULT '' COMMENT '地区（身份证）',
    `detail_address`      varchar(255)     NOT NULL DEFAULT '' COMMENT '街道/详细地址（身份证）',
    `build_in_flag`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `admin_flag`          tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否管理员（0：否，1：是）',
    `ga_secret`           varchar(255)     NOT NULL DEFAULT '' COMMENT 'GA密钥',
    `ga_bind_flag`        tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否GA绑定（0：否，1：是）',
    `salt`                bigint unsigned  NOT NULL DEFAULT 0 COMMENT '加签盐',
    `sign`                varchar(64)      NOT NULL DEFAULT '' COMMENT '签名',
    PRIMARY KEY (`id`),
    KEY `idx_job_number` (`job_number`) COMMENT '普通索引（工号）',
    KEY `idx_enabled_locked_account_expired_credentials_expired` (`enabled`, `locked`, `account_expired`, `credentials_expired`) COMMENT '普通索引（账号[是否启用、是否锁定、是否过期]，密码是否过期）',
    KEY `idx_username` (`username`) COMMENT '普通索引（用户名）',
    KEY `idx_mobile` (`mobile`) COMMENT '普通索引（手机）',
    KEY `idx_mail` (`mail`) COMMENT '普通索引（邮箱）',
    KEY `idx_id_card` (`id_card`) COMMENT '普通索引（身份证）'
) COMMENT ='用户表';

-- ----------------------------
-- Table structure for user_group
-- ----------------------------
DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group`
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
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
    KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='用户组表';

-- ----------------------------
-- Table structure for user_group_role
-- ----------------------------
DROP TABLE IF EXISTS `user_group_role`;
CREATE TABLE `user_group_role`
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
    `user_group_id`      bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户组主键',
    `role_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '角色主键',
    PRIMARY KEY (`id`),
    KEY `idx_user_group_id` (`user_group_id`) COMMENT '普通索引（用户组主键）',
    KEY `idx_role_id` (`role_id`) COMMENT '普通索引（角色主键）'
) COMMENT ='用户组角色关系表';

-- ----------------------------
-- Table structure for user_group_user
-- ----------------------------
DROP TABLE IF EXISTS `user_group_user`;
CREATE TABLE `user_group_user`
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
    `user_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户主键',
    `user_group_id`      bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户组主键',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '普通索引（用户主键）',
    KEY `idx_user_group_id` (`user_group_id`) COMMENT '普通索引（用户组主键）'
) COMMENT ='用户组用户关系表';

-- ----------------------------
-- Table structure for user_org
-- ----------------------------
DROP TABLE IF EXISTS `user_org`;
CREATE TABLE `user_org`
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
    `checked`            tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否选中（用于切换组织，0:否 1：是）',
    `user_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户主键',
    `org_id`             bigint unsigned  NOT NULL DEFAULT 0 COMMENT '组织主键',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '普通索引（用户主键）',
    KEY `idx_org_id` (`org_id`) COMMENT '普通索引（组织主键）'
) COMMENT ='用户组织关系表';

-- ----------------------------
-- Table structure for user_department
-- ----------------------------
DROP TABLE IF EXISTS `user_department`;
CREATE TABLE `user_department`
(
    `id`                 bigint unsigned NOT NULL COMMENT '主键',
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
    `user_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户主键',
    `department_id`      bigint unsigned  NOT NULL DEFAULT 0 COMMENT '部门主键',
    `primary_flag`       tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否主要部门（0：否，1：是）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '普通索引（用户主键）',
    KEY `idx_department_id` (`department_id`) COMMENT '普通索引（部门主键）'
) COMMENT ='用户部门关系表';

-- ----------------------------
-- Table structure for user_position
-- ----------------------------
DROP TABLE IF EXISTS `user_position`;
CREATE TABLE `user_position`
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
    `user_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户主键',
    `position_id`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '职位主键',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '普通索引（用户主键）',
    KEY `idx_position_id` (`position_id`) COMMENT '普通索引（职位主键）'
) COMMENT ='用户职位关系表';

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`
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
    `user_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户主键',
    `role_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '角色主键',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '普通索引（用户主键）',
    KEY `idx_role_id` (`role_id`) COMMENT '普通索引（角色主键）'
) COMMENT ='用户角色关系表';

-- ----------------------------
-- Table structure for user_extend_yubikey
-- ----------------------------
DROP TABLE IF EXISTS `user_extend_yubikey`;
CREATE TABLE `user_extend_yubikey`
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
    `user_id`            bigint unsigned  NOT NULL DEFAULT 0 COMMENT '用户主键',
    `yubikey_public_key` text             NOT NULL DEFAULT '' COMMENT 'yubikey公钥',
    `biz_type`           int unsigned     NOT NULL DEFAULT 0 COMMENT '业务类型（0：普通）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '普通索引（用户主键）'
) COMMENT ='用户扩展yubikey表';

SET FOREIGN_KEY_CHECKS = 1;
