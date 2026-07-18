SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for region
-- ----------------------------

DROP TABLE IF EXISTS `region`;
CREATE TABLE `region`
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
    `level`              int unsigned     NOT NULL DEFAULT 1 COMMENT '级别',
    `seq`                int unsigned     NOT NULL DEFAULT 1 COMMENT '排序号',
    `build_in_flag`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
    `parent_id`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '父类主键',
    PRIMARY KEY (`id`),
    KEY `idx_name_parent_id` (`name`, `parent_id`) COMMENT '复合索引（名称，父类主键）'
) COMMENT ='省市区表';

SET FOREIGN_KEY_CHECKS = 1;