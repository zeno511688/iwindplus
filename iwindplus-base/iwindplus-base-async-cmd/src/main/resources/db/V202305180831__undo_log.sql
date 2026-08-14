SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `branch_id` bigint UNSIGNED NOT NULL COMMENT '分支事务ID',
  `xid` varchar(128) NOT NULL COMMENT '全局事务ID',
  `context` varchar(128) NOT NULL COMMENT 'undo_log上下文，例如序列化',
  `rollback_info` longblob NOT NULL COMMENT '回滚信息',
  `log_status` int UNSIGNED NOT NULL COMMENT '0:正常状态，1:防御状态',
  `log_created` datetime NOT NULL COMMENT '创建时间',
  `log_modified` datetime NOT NULL COMMENT '修改时间',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) COMMENT='AT事务模式回滚表';

SET FOREIGN_KEY_CHECKS = 1;