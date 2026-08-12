-- ----------------------------
-- Table structure for async_cmd
-- ----------------------------
DROP TABLE IF EXISTS `async_cmd`;
CREATE TABLE `async_cmd`
(
    `id`                 bigint unsigned  NOT NULL COMMENT '主键',
    `created_timestamp`  bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
    `created_by`         varchar(50)      NOT NULL DEFAULT '' COMMENT '创建人',
    `created_id`         bigint unsigned  NOT NULL DEFAULT 0 COMMENT '创建人主键',
    `modified_timestamp` bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
    `modified_by`        varchar(50)      NOT NULL DEFAULT '' COMMENT '更新人',
    `modified_id`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '更新人主键',
    `deleted`            tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
    `version`            int unsigned     NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
    `remark`             varchar(255)     NOT NULL DEFAULT '' COMMENT '备注',
    `status`             int unsigned     NOT NULL DEFAULT 0 COMMENT '状态（0：待执行，10：执行中，20：异步等待，30：成功，40：失败）',
    `env`                varchar(20)      NOT NULL DEFAULT '' COMMENT '环境',
    `biz_name`           varchar(50)      NOT NULL DEFAULT '' COMMENT '业务名称',
    `biz_key`            varchar(50)      NOT NULL DEFAULT '' COMMENT '业务key，例如 ORDER',
    `biz_type`           varchar(50)      NOT NULL DEFAULT '' COMMENT '业务类型，例如 ORDER_CREATE',
    `biz_number`         varchar(50)      NOT NULL DEFAULT '' COMMENT '业务流水号，例如订单号',
    `dispatch_mode`      int unsigned     NOT NULL DEFAULT 0 COMMENT '调度模式（0：异步，1：调度中心，2：未知）',
    `execute_name`       varchar(100)     NOT NULL DEFAULT '' COMMENT '执行器名称',
    `expire_time`        bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '过期时间',
    `retry_count`        int unsigned     NOT NULL DEFAULT 0 COMMENT '重试次数',
    `next_retry_time`    bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '下次重试时间',
    `param`              text             NOT NULL DEFAULT '' COMMENT '参数',
    `error_msg`          text             NOT NULL DEFAULT '' COMMENT '错误信息',
    `sub_task_count`     int unsigned     NOT NULL DEFAULT 0 COMMENT '子任务总数',
    `cost_time`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '累计耗时',
    `need_callback`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否需要异步回调结果（0：否，1：是）',
    `callback_first`     tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否先回调（组任务模式：先主执行回调成功再分发子任务）',
    `need_display`       tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否需要显示（0：否，1：是）',
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
    `created_timestamp`  bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
    `created_by`         varchar(50)      NOT NULL DEFAULT '' COMMENT '创建人',
    `created_id`         bigint unsigned  NOT NULL DEFAULT 0 COMMENT '创建人主键',
    `modified_timestamp` bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
    `modified_by`        varchar(50)      NOT NULL DEFAULT '' COMMENT '更新人',
    `modified_id`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '更新人主键',
    `deleted`            tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
    `version`            int unsigned     NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
    `remark`             varchar(255)     NOT NULL DEFAULT '' COMMENT '备注',
    `status`             int unsigned     NOT NULL DEFAULT 0 COMMENT '状态（0：待执行，10：执行中，20：异步等待，30：成功，40：失败，50：废弃）',
    `biz_name`           varchar(50)      NOT NULL DEFAULT '' COMMENT '业务名称',
    `biz_key`            varchar(50)      NOT NULL DEFAULT '' COMMENT '业务key，例如 ORDER',
    `biz_type`           varchar(50)      NOT NULL DEFAULT '' COMMENT '业务类型，例如 ORDER_CREATE',
    `biz_number`         varchar(50)      NOT NULL DEFAULT '' COMMENT '业务流水号，例如订单号',
    `stage`              int(10) unsigned NOT NULL DEFAULT 0 COMMENT '阶段（同阶段子任务并发）',
    `seq`                int(10) unsigned NOT NULL DEFAULT 1 COMMENT '排序号',
    `execute_name`       varchar(100)     NOT NULL DEFAULT '' COMMENT '执行器名称',
    `retry_count`        int unsigned     NOT NULL DEFAULT 0 COMMENT '重试次数',
    `param`              text             NOT NULL DEFAULT '' COMMENT '参数',
    `result`             text             NOT NULL DEFAULT '' COMMENT '结果（供后续任务读取，同一批互相不可见，由于是并发）',
    `error_msg`          text             NOT NULL DEFAULT '' COMMENT '错误信息',
    `cost_time`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '耗时',
    `need_callback`      tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否需要异步回调结果（0：否，1：是）',
    `expire_time`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '回调等待截止时间',
    `need_display`       tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否需要显示（0：否，1：是）',
    `async_cmd_id`       bigint unsigned  NOT NULL DEFAULT 0 COMMENT '异步命令主键',
    PRIMARY KEY (`id`),
    KEY `idx_async_cmd_id_seq_status` (`async_cmd_id`, `seq`, `status`) COMMENT '复合索引（异步命令主键, 排序号, 状态）'
) COMMENT ='异步命令子表';
DROP TABLE IF EXISTS `async_cmd_sub`;
