-- ----------------------------
-- Table structure for async_cmd
-- ----------------------------
DROP TABLE IF EXISTS `async_cmd`;
CREATE TABLE `async_cmd`
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
    `status`             int unsigned     NOT NULL DEFAULT 0 COMMENT '状态（0：待执行，1：执行中，2：成功，3：失败，4：废弃）',
    `env`                varchar(20)      NOT NULL DEFAULT '' COMMENT '环境',
    `biz_key`            varchar(50)      NOT NULL DEFAULT '' COMMENT '业务key，例如 ORDER',
    `biz_type`           varchar(50)      NOT NULL DEFAULT '' COMMENT '业务类型，例如 ORDER_CREATE',
    `biz_number`         varchar(50)      NOT NULL DEFAULT '' COMMENT '业务流水号，例如订单号',
    `dispatch_mode`      int unsigned     NOT NULL DEFAULT 0 COMMENT '调度模式（0：异步，1：调度中心，2：未知）',
    `execute_name`       varchar(50)      NOT NULL DEFAULT '' COMMENT '执行器名称',
    `expire_time`        datetime         NOT NULL DEFAULT current_timestamp() COMMENT '过期时间',
    `retry_count`        int unsigned     NOT NULL DEFAULT 0 COMMENT '重试次数',
    `next_retry_time`    datetime         NOT NULL DEFAULT current_timestamp() COMMENT '下次重试时间',
    `content`            text             NOT NULL DEFAULT '' COMMENT '内容',
    `error_msg`          text             NOT NULL DEFAULT '' COMMENT '错误信息',
    `sub_task_count`     int unsigned     NOT NULL DEFAULT 0 COMMENT '子任务总数',
    `cost_time`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '累计耗时',
    PRIMARY KEY (`id`),
    KEY `idx_env_biz_number` (`env`, `biz_number`) COMMENT '复合索引（环境, 业务流水号）',
    KEY `idx_env_biz_key_biz_type` (`env`, `biz_key`, `biz_type`) COMMENT '复合索引（环境，业务key，业务类型）'
) COMMENT ='异步命令表';

-- ----------------------------
-- Table structure for async_cmd_sub
-- ----------------------------
DROP TABLE IF EXISTS `async_cmd_sub`;
CREATE TABLE `async_cmd_sub`
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
    `status`             int unsigned     NOT NULL DEFAULT 0 COMMENT '状态（0：待执行，1：执行中，2：成功，3：失败，4：废弃）',
    `biz_type`           varchar(50)      NOT NULL DEFAULT '' COMMENT '业务类型，例如 ORDER_CREATE',
    `seq`                int(10) unsigned NOT NULL DEFAULT 1 COMMENT '排序号',
    `execute_name`       varchar(50)      NOT NULL DEFAULT '' COMMENT '执行器名称',
    `retry_count`        int unsigned     NOT NULL DEFAULT 0 COMMENT '重试次数',
    `content`            text             NOT NULL DEFAULT '' COMMENT '内容',
    `error_msg`          text             NOT NULL DEFAULT '' COMMENT '错误信息',
    `cost_time`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '耗时',
    `async_cmd_id`       bigint unsigned  NOT NULL DEFAULT 0 COMMENT '异步命令主键',
    PRIMARY KEY (`id`),
    KEY `idx_async_cmd_id_seq_status` (`async_cmd_id`, `seq`, `status`) COMMENT '复合索引（异步命令主键, 排序号, 状态）'
) COMMENT ='异步命令子表';