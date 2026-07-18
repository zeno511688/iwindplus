-- ----------------------------
-- Table structure for tcc_global_tx
-- ----------------------------
DROP TABLE IF EXISTS `tcc_global_tx`;
CREATE TABLE `tcc_global_tx`
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
    `status`             int unsigned     NOT NULL DEFAULT 0 COMMENT '状态（0：Try尝试中，1：Confirm处理中，2：Confirm成功，3：Confirm失败，4：Cancel处理中，5：Cancel成功，6：Cancel失败',
    `xid`                varchar(50)      NOT NULL DEFAULT '' COMMENT '全局事务ID',
    `biz_type`           varchar(64)      NOT NULL DEFAULT '' COMMENT '业务类型',
    `env`                varchar(20)      NOT NULL DEFAULT '' COMMENT '环境',
    `timeout_seconds`    int unsigned     NOT NULL DEFAULT 10 COMMENT '超时时间',
    `expire_time`        datetime         NOT NULL DEFAULT current_timestamp() COMMENT '过期时间',
    `retry_count`        int unsigned     NOT NULL DEFAULT 0 COMMENT '重试次数',
    `next_retry_time`    datetime         NOT NULL DEFAULT current_timestamp() COMMENT '下次重试时间',
    PRIMARY KEY (`id`),
    KEY `idx_xid` (`xid`) COMMENT '普通索引（全局事务ID）',
    KEY `idx_biz_type` (`biz_type`) COMMENT '普通索引（业务类型）'
) COMMENT ='tcc全局事务表';

-- ----------------------------
-- Table structure for tcc_branch_tx
-- ----------------------------
DROP TABLE IF EXISTS `tcc_branch_tx`;
CREATE TABLE `tcc_branch_tx`
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
    `status`             int unsigned     NOT NULL DEFAULT 0 COMMENT '状态（0：Try尝试中，1：Try成功，2：Try失败，3：Confirm处理中，4：Confirm成功，5：Confirm失败，6：Cancel处理中，7：Cancel成功，8：Cancel失败',
    `xid`                varchar(50)      NOT NULL DEFAULT '' COMMENT '全局事务ID',
    `branch_id`          bigint(20)       NOT NULL DEFAULT 1 COMMENT '分支ID',
    `context_path`       varchar(50)      NOT NULL DEFAULT '' COMMENT '上下文路径',
    `confirm_url`        VARCHAR(255)     NOT NULL DEFAULT '' COMMENT 'Confirm 回调地址',
    `cancel_url`         VARCHAR(255)     NOT NULL DEFAULT '' COMMENT 'Cancel 回调地址',
    `payload`            text             NOT NULL DEFAULT '' COMMENT '参数',
    `error_msg`          text             NOT NULL DEFAULT '' COMMENT '错误信息',
    PRIMARY KEY (`id`),
    KEY `idx_xid` (`xid`, `branch_id`) COMMENT '复合索引（全局事务ID，分支ID）'
) COMMENT ='tcc分支事务表';