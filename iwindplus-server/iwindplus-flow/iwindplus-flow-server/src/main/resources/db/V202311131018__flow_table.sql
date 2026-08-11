SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for flow_category
-- ----------------------------
DROP TABLE IF EXISTS `flow_category`;
CREATE TABLE `flow_category` (
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
  `seq` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '排序号',
  `build_in_flag` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='流程分类表';

-- ----------------------------
-- Table structure for flow_form
-- ----------------------------
DROP TABLE IF EXISTS `flow_form`;
CREATE TABLE `flow_form` (
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
  `content` text DEFAULT NULL COMMENT '表单内容',
  `seq` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '排序号',
  `build_in_flag` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='流程表单表';

-- ----------------------------
-- Table structure for flow_his_instance
-- ----------------------------
DROP TABLE IF EXISTS `flow_his_instance`;
CREATE TABLE `flow_his_instance` (
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
  `status` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '状态（0：审批中，1：已审核，2：已驳回，3：已撤销，4：已终止）',
  `code` varchar(50) NOT NULL DEFAULT '' COMMENT '编码',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `biz_number` varchar(50) NOT NULL DEFAULT '' COMMENT '业务流水号',
  `current_node_code` varchar(50) NOT NULL DEFAULT '' COMMENT '当前节点编码',
  `current_node_name` varchar(50) NOT NULL DEFAULT '' COMMENT '当前节点名称',
  `take_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '耗时',
  `model_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '模型主键',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
  KEY `idx_biz_number` (`biz_number`) COMMENT '普通索引（业务流水号）',
  KEY `idx_model_id` (`model_id`) COMMENT '普通索引（模型主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='历史流程实例表';

-- ----------------------------
-- Table structure for flow_his_instance_extend
-- ----------------------------
DROP TABLE IF EXISTS `flow_his_instance_extend`;
CREATE TABLE `flow_his_instance_extend` (
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
  `variable` text DEFAULT NULL COMMENT '变量',
  `instance_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '实例主键',
  PRIMARY KEY (`id`),
  KEY `idx_instance_id` (`instance_id`) COMMENT '普通索引（实例主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='历史流程实例扩展表';


-- ----------------------------
-- Table structure for flow_his_task
-- ----------------------------
DROP TABLE IF EXISTS `flow_his_task`;
CREATE TABLE `flow_his_task` (
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
  `status` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '状态（0：审批中，1：已审核，2：已驳回，3：已撤销，4：已终止）',
  `code` varchar(50) NOT NULL DEFAULT '' COMMENT '编码',
  `node_code` varchar(50) NOT NULL DEFAULT '' COMMENT '节点编码',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `type` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '类型',
  `comment` varchar(255) NOT NULL DEFAULT '' COMMENT '审批意见',
  `take_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '耗时',
  `instance_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '实例主键',
  `model_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '模型主键',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_node_code` (`node_code`) COMMENT '普通索引（节点编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
  KEY `idx_instance_id` (`instance_id`) COMMENT '普通索引（实例主键）',
  KEY `idx_model_id` (`model_id`) COMMENT '普通索引（模型主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='历史流程任务表';

-- ----------------------------
-- Records of flow_his_task
-- ----------------------------
BEGIN;
INSERT INTO `flow_his_task` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `node_code`, `name`, `type`, `comment`, `take_time`, `instance_id`, `model_id`) VALUES (2058171248849580034, 1779541760380, 'system', 1543960516026822658, 1779541760380, 'system', 1543960516026822658, 0, 0, '', 1, 'FT260523000019', 'NODE_DEPT_MANAGER', '部门负责人审批', 0, '', 521378, 2058171248832802817, 2057486223015501825);
INSERT INTO `flow_his_task` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `node_code`, `name`, `type`, `comment`, `take_time`, `instance_id`, `model_id`) VALUES (2058174780357079041, 1779542398617, 'system', 1543960516026822658, 1779542398617, 'system', 1543960516026822658, 0, 0, '', 1, 'FT260523000020', 'NODE_DEPT_MANAGER', '部门负责人审批', 0, '', 311996, 2058174780319330306, 2057486223015501825);
INSERT INTO `flow_his_task` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `node_code`, `name`, `type`, `comment`, `take_time`, `instance_id`, `model_id`) VALUES (2058176544124801026, 1779542806443, 'system', 1543960516026822658, 1779542806443, 'system', 1543960516026822658, 0, 0, '', 1, 'FT260523000021', 'NODE_DEPT_MANAGER', '部门负责人审批', 0, '', 305391, 2058176544074469378, 2057486223015501825);
INSERT INTO `flow_his_task` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `node_code`, `name`, `type`, `comment`, `take_time`, `instance_id`, `model_id`) VALUES (2058186634479382530, 1779544934158, 'system', 1543960516026822658, 1779544934158, 'system', 1543960516026822658, 0, 0, '', 1, 'FT260523000022', 'NODE_DEPT_MANAGER', '部门负责人审批', 0, '', 27156, 2058186634424856578, 2057486223015501825);
INSERT INTO `flow_his_task` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `node_code`, `name`, `type`, `comment`, `take_time`, `instance_id`, `model_id`) VALUES (2058866003180146689, 1779706956037, 'system', 1543960516026822658, 1779706956037, 'system', 1543960516026822658, 0, 0, '', 1, 'FT260525000001', 'NODE_DEPT_MANAGER', '部门负责人审批', 0, '', 75035, 2058866003117232129, 2057486223015501825);
INSERT INTO `flow_his_task` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `node_code`, `name`, `type`, `comment`, `take_time`, `instance_id`, `model_id`) VALUES (2066351937902129154, 1781492186585, 'system', 1543960516026822658, 1781492186585, 'system', 1543960516026822658, 0, 0, '', 1, 'FT260615000001', 'NODE_DEPT_MANAGER', '部门负责人审批', 0, '', 519584, 2066351937847603201, 2057486223015501825);
COMMIT;

-- ----------------------------
-- Table structure for flow_his_task_player
-- ----------------------------
DROP TABLE IF EXISTS `flow_his_task_player`;
CREATE TABLE `flow_his_task_player` (
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
  `player_name` varchar(50) NOT NULL DEFAULT '' COMMENT '参与人名称',
  `type` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '类型',
  `seq` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '排序号',
  `player_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '参与人主键',
  `task_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '任务主键',
  `instance_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '实例主键',
  `model_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '模型主键',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`) COMMENT '普通索引（参与人主键）',
  KEY `idx_task_id` (`task_id`) COMMENT '普通索引（任务主键）',
  KEY `idx_instance_id` (`instance_id`) COMMENT '普通索引（实例主键）',
  KEY `idx_model_id` (`model_id`) COMMENT '普通索引（模型主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='历史流程任务参与人';

-- ----------------------------
-- Table structure for flow_instance
-- ----------------------------
DROP TABLE IF EXISTS `flow_instance`;
CREATE TABLE `flow_instance` (
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
  `code` varchar(50) NOT NULL DEFAULT '' COMMENT '编码',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `biz_number` varchar(50) NOT NULL DEFAULT '' COMMENT '业务流水号',
  `current_node_code` varchar(50) NOT NULL DEFAULT '' COMMENT '当前节点编码',
  `current_node_name` varchar(50) NOT NULL DEFAULT '' COMMENT '当前节点名称',
  `model_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '模型主键',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
  KEY `idx_biz_number` (`biz_number`) COMMENT '普通索引（业务流水号）',
  KEY `idx_model_id` (`model_id`) COMMENT '普通索引（模型主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='流程实例表';

-- ----------------------------
-- Table structure for flow_instance_callback
-- ----------------------------
DROP TABLE IF EXISTS `flow_instance_callback`;
CREATE TABLE `flow_instance_callback` (
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
  `status` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '状态（0：待处理，1：完成，2：失败，丢弃）',
  `category_name` varchar(50) NOT NULL DEFAULT '' COMMENT '分类名称',
  `model_name` varchar(50) NOT NULL DEFAULT '' COMMENT '模型名称',
  `instance_name` varchar(50) NOT NULL DEFAULT '' COMMENT '实例名称',
  `biz_number` varchar(50) NOT NULL DEFAULT '' COMMENT '业务流水号',
  `callback_url` varchar(50) NOT NULL DEFAULT '' COMMENT '回调地址',
  `variable` text DEFAULT NULL COMMENT '变量',
  `retry_count` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '重试次数',
  `category_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '分类主键',
  `model_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '模型主键',
  `instance_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '实例主键',
  PRIMARY KEY (`id`),
  KEY `idx_category_name` (`category_name`) COMMENT '普通索引（分类名称名称）',
  KEY `idx_model_name` (`model_name`) COMMENT '普通索引（模型名称）',
  KEY `idx_instance_name` (`instance_name`) COMMENT '普通索引（实例名称）',
  KEY `idx_biz_number` (`biz_number`) COMMENT '普通索引（业务流水号）',
  KEY `idx_category_id` (`category_id`) COMMENT '普通索引（分类主键）',
  KEY `idx_model_id` (`model_id`) COMMENT '普通索引（模型主键）',
  KEY `idx_instance_id` (`instance_id`) COMMENT '普通索引（实例主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='流程实例回调表';

-- ----------------------------
-- Table structure for flow_instance_extend
-- ----------------------------
DROP TABLE IF EXISTS `flow_instance_extend`;
CREATE TABLE `flow_instance_extend` (
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
  `variable` text DEFAULT NULL COMMENT '变量',
  `instance_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '实例主键',
  PRIMARY KEY (`id`),
  KEY `idx_instance_id` (`instance_id`) COMMENT '普通索引（实例主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='流程实例扩展表';

-- ----------------------------
-- Table structure for flow_model
-- ----------------------------
DROP TABLE IF EXISTS `flow_model`;
CREATE TABLE `flow_model` (
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
  `status` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '状态（0：待发布，1：已发布，2：已停用，3：历史版本）',
  `code` varchar(50) NOT NULL DEFAULT '' COMMENT '编码',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `model_version` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '模型版本',
  `form_type` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '表单类型（0：表单，1：自定义，2：固定格式）',
  `form_path` varchar(255) NOT NULL DEFAULT '' COMMENT '表单路径',
  `seq` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '排序号',
  `build_in_flag` tinyint(3) unsigned NOT NULL DEFAULT 0 COMMENT '是否内置（0：否，1：是）',
  `category_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '分类主键',
  `form_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '表单主键',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
  KEY `idx_category_id` (`category_id`) COMMENT '普通索引（分类主键）',
  KEY `idx_form_id` (`form_id`) COMMENT '普通索引（表单主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='流程模型表';

-- ----------------------------
-- Table structure for flow_model_extend
-- ----------------------------
DROP TABLE IF EXISTS `flow_model_extend`;
CREATE TABLE `flow_model_extend` (
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
  `form_content` text DEFAULT NULL COMMENT '表单内容',
  `model_content` text DEFAULT NULL COMMENT '模型内容',
  `model_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '模型主键',
  PRIMARY KEY (`id`),
  KEY `idx_model_id` (`model_id`) COMMENT '普通索引（模型主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='流程模型扩展表';

-- ----------------------------
-- Table structure for flow_task
-- ----------------------------
DROP TABLE IF EXISTS `flow_task`;
CREATE TABLE `flow_task` (
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
  `code` varchar(50) NOT NULL DEFAULT '' COMMENT '编码',
  `node_code` varchar(50) NOT NULL DEFAULT '' COMMENT '节点编码',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `type` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '类型',
  `comment` varchar(255) NOT NULL DEFAULT '' COMMENT '审批意见',
  `instance_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '实例主键',
  `model_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '模型主键',
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`) COMMENT '普通索引（编码）',
  KEY `idx_node_code` (`node_code`) COMMENT '普通索引（节点编码）',
  KEY `idx_name` (`name`) COMMENT '普通索引（名称）',
  KEY `idx_instance_id` (`instance_id`) COMMENT '普通索引（实例主键）',
  KEY `idx_model_id` (`model_id`) COMMENT '普通索引（模型主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='流程任务表';

-- ----------------------------
-- Table structure for flow_task_player
-- ----------------------------
DROP TABLE IF EXISTS `flow_task_player`;
CREATE TABLE `flow_task_player` (
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
  `player_name` varchar(50) NOT NULL DEFAULT '' COMMENT '参与人名称',
  `type` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '类型',
  `seq` int(10) unsigned NOT NULL DEFAULT 1 COMMENT '排序号',
  `player_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '参与人主键',
  `task_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '任务主键',
  `instance_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '实例主键',
  `model_id` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '模型主键',
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`) COMMENT '普通索引（参与人主键）',
  KEY `idx_task_id` (`task_id`) COMMENT '普通索引（任务主键）',
  KEY `idx_instance_id` (`instance_id`) COMMENT '普通索引（实例主键）',
  KEY `idx_model_id` (`model_id`) COMMENT '普通索引（模型主键）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='流程任务参与人';

SET FOREIGN_KEY_CHECKS = 1;
