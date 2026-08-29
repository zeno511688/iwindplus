-- ----------------------------
-- Table structure for export_task
-- ----------------------------
DROP TABLE IF EXISTS `export_task`;
CREATE TABLE `export_task`
(
    `id`                 bigint unsigned  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `created_timestamp`  bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '创建时间戳',
    `created_by`         varchar(50)      NOT NULL DEFAULT '' COMMENT '创建人',
    `created_id`         bigint unsigned  NOT NULL DEFAULT 0 COMMENT '创建人主键',
    `modified_timestamp` bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '更新时间戳',
    `modified_by`        varchar(50)      NOT NULL DEFAULT '' COMMENT '更新人',
    `modified_id`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '更新人主键',
    `deleted`            tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除（0：未删除，1：已删除）',
    `version`            int unsigned     NOT NULL DEFAULT 0 COMMENT '乐观锁（处理并发）',
    `remark`             varchar(255)     NOT NULL DEFAULT '' COMMENT '备注',
    `status`             int unsigned     NOT NULL DEFAULT 0 COMMENT '状态（0：待执行，10：执行中，20：成功，30：失败，40：丢弃）',
    `biz_number`         varchar(50)      NOT NULL DEFAULT '' COMMENT '业务流水号，例如订单号',
    `file_name`          varchar(255)     NOT NULL DEFAULT NULL COMMENT '文件名',
    `file_path`          varchar(500)     NOT NULL DEFAULT NULL COMMENT '文件路径',
    `execute_name`       varchar(100)     NOT NULL DEFAULT '' COMMENT '执行器名称',
    `query_param`        text             NOT NULL DEFAULT '' COMMENT '查询参数',
    `total_count`        bigint unsigned  NOT NULL DEFAULT 0 COMMENT '导出数据总数',
    `exported_count`     bigint unsigned  NOT NULL DEFAULT 0 COMMENT '已导出数量',
    `expire_time`        bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '过期时间',
    `retry_count`        int unsigned     NOT NULL DEFAULT 0 COMMENT '重试次数',
    `next_retry_time`    bigint unsigned  NOT NULL DEFAULT (unix_timestamp() * 1000) COMMENT '下次重试时间',
    `error_msg`          text             NOT NULL DEFAULT '' COMMENT '错误信息',
    `cost_time`          bigint unsigned  NOT NULL DEFAULT 0 COMMENT '耗时',
    `progress`           int unsigned     NOT NULL DEFAULT 0 COMMENT '进度比例（0-100）',
    `ext`                text             NOT NULL DEFAULT '' COMMENT '扩展字段（JSON格式）',
    PRIMARY KEY (`id`),
    KEY `idx_biz_number` (`biz_number`) COMMENT '普通索引（业务流水号）'
) COMMENT ='导出任务表';
