SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Records of mail_config
-- ----------------------------
BEGIN;
INSERT INTO `mail_config` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `nick_name`, `host`, `username`, `password`, `port`, `ssl_enable`, `retry_enable`, `build_in_flag`, `org_id`) VALUES (1543959717166981122, '2020-04-28 08:51:35', 1757785403815, 'system', 1543960516026822658, '2025-06-25 10:20:37', 1757785403815, 'system', 1543960516026822658, 0, 18, '163邮箱配置', 1, 'be1af6033a9371f20b4fbc42817fbe70', '163邮箱配置', '爱丰信息技术', 'smtp.163.com', 'zengdeguiu88@163.com', 'Gqo/ok/q5RY7JDFiaqCzsYdHmqG3/bJ67CluiSdvw/GscCiSUNQsSIVclfNkTg==', 465, 1, 1, 0, 1543953063620931586);
INSERT INTO `mail_config` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `nick_name`, `host`, `username`, `password`, `port`, `ssl_enable`, `retry_enable`, `build_in_flag`, `org_id`) VALUES (1543959781390159873, '2021-06-08 14:29:24', 1757785403815, 'system', 1543960516026822658, '2025-06-25 10:24:28', 1757785403815, 'system', 1543960516026822658, 0, 9, 'qq邮箱配置', 1, '8adb9576adfc259777a0ba1d1998541b', 'QQ邮箱配置', '爱丰信息技术', 'smtp.qq.com', '845875725@qq.com', 'aGWIAwDWrSU8Ka5JBroQ7/TkPO7ptE7w1WRUrKloQSuBKpUhs++1Toa1X/A=', 465, 1, 1, 0, 1543953063620931586);
COMMIT;

-- ----------------------------
-- Records of mail_tpl
-- ----------------------------
BEGIN;
INSERT INTO `mail_tpl` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `template_content`, `captcha_timeout`, `captcha_length`, `limit_count_day`, `limit_count_hour`, `limit_count_minute`, `build_in_flag`, `config_id`, `org_id`) VALUES (1543959629879328770, '2021-12-10 13:22:34', 1757785403815, 'system', 1543960516026822658, '2025-06-23 18:31:40', 1757785403815, 'system', 1543960516026822658, 0, 7, '163邮件验证码', 1, 'c3f67fd355dd6098156053f68285ba3e', '163邮件验证码', '验证码： ${captcha}，${timeout}分钟内有效，误泄露给他人，如非本人操作请忽略。', 10, 6, 10, 5, 1, 0, 1543959717166981122, 1543953063620931586);
INSERT INTO `mail_tpl` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `template_content`, `captcha_timeout`, `captcha_length`, `limit_count_day`, `limit_count_hour`, `limit_count_minute`, `build_in_flag`, `config_id`, `org_id`) VALUES (1543959629879328771, '2021-12-10 13:22:34', 1757785403815, 'system', 1543960516026822658, '2025-06-23 18:32:00', 1757785403815, 'system', 1543960516026822658, 0, 9, 'qq邮件验证码', 1, 'c3f67fd355dd6098156053f68285ba3f', 'qq邮件验证码', '验证码： ${captcha}，${timeout}分钟内有效，误泄露给他人，如非本人操作请忽略。', 10, 6, 20, 5, 1, 0, 1543959781390159873, 1543953063620931586);
COMMIT;

-- ----------------------------
-- Records of oss_config
-- ----------------------------
BEGIN;
INSERT INTO `oss_config` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `oss_endpoint`, `access_key`, `secret_key`, `sts_endpoint`, `role_arn`, `policy`, `build_in_flag`, `org_id`) VALUES (1692020536010055681, '2023-08-17 11:48:26', 1757785403815, 'system', 1543960516026822658, '2025-06-25 10:21:24', 1757785403815, 'system', 1543960516026822658, 0, 5, '阿里云对象存储配置', 1, 1, '7ebc67de2d76c44d3cc4a2bec55b22a1', '阿里云对象存储配置', 'https://oss-cn-shenzhen.aliyuncs.com', 'LTAI5tEsX3sewUecFJxuWZ7S', 'T8FUGlnUE/jclZi8IoIxBmI9zNphnT4t5CExCWzHLteMF7DLm6wguAsFWSmck0YyJK/gouWtg1PR4g==', 'sts.cn-shenzhen.aliyuncs.com', 'acs:ram::1277790089989807:role/oss-full', '', 0, 1543953063620931586);
INSERT INTO `oss_config` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `oss_endpoint`, `access_key`, `secret_key`, `sts_endpoint`, `role_arn`, `policy`, `build_in_flag`, `org_id`) VALUES (1692020536010055682, '2023-08-17 11:48:26', 1757785403815, 'system', 1543960516026822658, '2025-06-25 10:22:07', 1757785403815, 'system', 1543960516026822658, 0, 3, 'minio对象存储配置', 1, 0, 'baa4c3273d536fb5111ebb086450bece', 'minio对象存储配置', 'http://127.0.0.1:9010', 'k3AHuk3bgQhv1lNNPaNt', '2cFZc9tg0Nuq4J3W2nIOPoihI+HbWnQejhVrfxQdQl3AYmzOqcDoIwoS9hpbtZ8GOegpCyi+CD5PmeMTN1Fo5Dc6yGQ=', '', '', '', 0, 1543953063620931586);
COMMIT;

-- ----------------------------
-- Records of oss_tpl
-- ----------------------------
BEGIN;
INSERT INTO `oss_tpl` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `bucket_name`, `access_domain`, `part_size`, `broke`, `build_in_flag`, `config_id`, `org_id`) VALUES (1543959629879323572, '2021-12-10 13:22:34', 1757785403815, 'system', 1543960516026822658, '2025-06-23 18:49:26', 1757785403815, 'system', 1543960516026822658, 0, 7, '管理服务上传空间名', 1, 'c3f67fd355dd6098156053f68385ba32', '阿里云管理服务空间名', 'iwindplus-mgt', 'https://iwindplus-mgt.oss-cn-shenzhen.aliyuncs.com', 200, 1, 0, 1692020536010055681, 1543953063620931586);
INSERT INTO `oss_tpl` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `bucket_name`, `access_domain`, `part_size`, `broke`, `build_in_flag`, `config_id`, `org_id`) VALUES (1543959629879323573, '2021-12-10 13:22:34', 1757785403815, 'system', 1543960516026822658, '2025-06-23 19:35:02', 1757785403815, 'system', 1543960516026822658, 0, 11, '即时通讯上传空间名', 1, 'c3f67fd355dd6098156053f68385ba33', '阿里云即时通讯空间名', 'iwindplus-im', 'https://iwindplus-im.oss-cn-shenzhen.aliyuncs.com', 200, 1, 0, 1692020536010055681, 1543953063620931586);
INSERT INTO `oss_tpl` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `bucket_name`, `access_domain`, `part_size`, `broke`, `build_in_flag`, `config_id`, `org_id`) VALUES (1543959629879323574, '2021-12-10 13:22:34', 1757785403815, 'system', 1543960516026822658, '2025-06-23 18:49:30', 1757785403815, 'system', 1543960516026822658, 0, 7, '管理服务上传空间名', 1, 'c3f67fd355dd6098156053f68385ba34', 'minio管理服务空间名', 'iwindplus-mgt', '', 200, 50, 0, 1692020536010055682, 1543953063620931586);
INSERT INTO `oss_tpl` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `bucket_name`, `access_domain`, `part_size`, `broke`, `build_in_flag`, `config_id`, `org_id`) VALUES (1543959629879323575, '2021-12-10 13:22:34', 1757785403815, 'system', 1543960516026822658, '2023-09-23 21:14:32', 1757785403815, 'system', 1543960516026822658, 0, 7, '即时通讯服务上传空间名', 1, 'c3f67fd355dd6098156053f68385ba35', 'minio即时通讯空间名', 'iwindplus-im', '', 200, 50, 1, 1692020536010055682, 1543953063620931586);
COMMIT;

-- ----------------------------
-- Records of sms_config
-- ----------------------------
BEGIN;
INSERT INTO `sms_config` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `access_key`, `secret_key`, `sts_endpoint`, `role_arn`, `policy`, `build_in_flag`, `org_id`) VALUES (1543959044379664385, '2023-08-17 11:23:57', 1757785403815, 'system', 1543960516026822658, '2025-06-25 10:24:19', 1757785403815, 'system', 1543960516026822658, 0, 6, '阿里云短信配置', 1, 0, '6856e64cc228215d44c1a351454a8cc5', '阿里云短信配置', 'LTAI5tMPRextB5gaVoyhRoyw', '8FIqRJhgXXrB6b9Z5IQdSTKbsGDKazODYh8/QDGUUDscrxJvg4VJoYPJXvdsGhHud9MD9eH36qxlEA==', 'sts.cn-shenzhen.aliyuncs.com', 'acs:ram::1277790089989807:role/sms-full', '', 0, 1543953063620931586);
COMMIT;

-- ----------------------------
-- Records of sms_tpl
-- ----------------------------
BEGIN;
INSERT INTO `sms_tpl` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `sign_name`, `template_content`, `captcha_timeout`, `captcha_length`, `limit_count_day`, `limit_count_hour`, `limit_count_minute`, `build_in_flag`, `config_id`, `org_id`) VALUES (1543959629879324283, '2021-12-10 13:22:34', 1757785403815, 'system', 1543960516026822658, '2025-06-23 18:49:46', 1757785403815, 'system', 1543960516026822658, 0, 7, '验证码（验证码）', 1, 'c3f67fd354dd6098154053f68285ba35', '阿里云-验证码（验证码）', 'iwindplus', 'SMS_232891215', 10, 6, 10, 5, 1, 0, 1543959044379664385, 1543953063620931586);
INSERT INTO `sms_tpl` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `sign_name`, `template_content`, `captcha_timeout`, `captcha_length`, `limit_count_day`, `limit_count_hour`, `limit_count_minute`, `build_in_flag`, `config_id`, `org_id`) VALUES (1543959629879324284, '2021-12-10 13:22:34', 1757785403815, 'system', 1543960516026822658, '2025-06-23 21:53:54', 1757785403815, 'system', 1543960516026822658, 0, 10, '敏感操作（验证码）', 1, 'c3f67fd354dd6098154053f68285ba35', '阿里云-敏感操作（验证码）', 'iwindplus', 'SMS_232891230', 10, 6, 10, 5, 1, 0, 1543959044379664385, 1543953063620931586);
COMMIT;

-- ----------------------------
-- Records of vod_config
-- ----------------------------
BEGIN;
INSERT INTO `vod_config` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `region`, `access_key`, `secret_key`, `sts_endpoint`, `role_arn`, `policy`, `notify_url`, `build_in_flag`, `org_id`) VALUES (1692021424917291009, '2023-08-17 11:51:58', 1757785403815, 'system', 1543960516026822658, '2025-06-23 18:58:17', 1757785403815, 'system', 1543960516026822658, 0, 4, '深圳视频点播', 1, 0, 'adb8e8a91200ba83461c341e7e07d68d', '深圳视频点播配置1', 'cn-shenzhen', 'LTAI5tDNKvicAnd1mgXgW4PW', 'l1bkqW4C1X6z4ilH+2KocFTygb7XYCZ6B6PxKy8YJgPW9YbEt05dMXhQGFYBGmsqdkj8aXuJnnXy6w==', 'sts.cn-shenzhen.aliyuncs.com', 'acs:ram::1277790089989807:role/vod-full', '', '', 0, 1543953063620931586);
COMMIT;

-- ----------------------------
-- Records of wechat_config_ma
-- ----------------------------
BEGIN;
INSERT INTO `wechat_config_ma` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `access_key`, `secret_key`, `token`, `aes_key`, `msg_data_format`, `use_redis`, `qrcode`, `notify_success_url`, `build_in_flag`, `org_id`) VALUES (1543959267093041153, '2021-08-31 19:55:31', 1757785403815, 'system', 1543960516026822658, '2025-06-25 10:46:43', 1757785403815, 'system', 1543960516026822658, 0, 34, '中都云谷微信小程序配置', 1, 'e5cc634902c1777b8d72a14ef1206f5c', '微信小程序配置', 'wxda58b0f32beeef30', 'lDFsUzXmStwPOwi83Ca34pqnsRagENg0Jau4YKnuerY2P70GSThHBaAu2/VdrfgWBSgGAFhXJir4lurpOA==', 'gx6MffkV9Y7Atw0ABt0xfPgIj6G6m54Puv9n45LGkK5', 'gx6MffkV9Y7Atw0ABt0xfPgIj6G6m54Puv9n45LGkK5', 'JSON', 1, 'pic/wechat/20211122/2c208d66272c4638ae23fc0bdf323da2.jpg', '', 0, 1543953063620931586);
COMMIT;

-- ----------------------------
-- Records of wechat_config_mp
-- ----------------------------
BEGIN;
INSERT INTO `wechat_config_mp` (`id`, `created_time`, `created_timestamp`, `created_by`, `created_id`, `modified_time`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `access_key`, `secret_key`, `token`, `aes_key`, `use_redis`, `notify_url`, `notify_success_url`, `build_in_flag`, `org_id`) VALUES (1692023005431701505, '2023-08-17 11:58:15', 1757785403815, 'system', 1543960516026822658, '2025-06-25 10:24:13', 1757785403815, 'system', 1543960516026822658, 0, 4, 'iwindplus管理后台（扫码登录）', 1, 'f37777e3d946693ba192b7744be8a670', '扫码登录', 'wxc6f9ff203eabffe0', 'I5+2hMAEQfitXrLRWC+9Frhui7rQCFOwnRkw7c759tjFsOA0xD8bJnLHhxxFlJildVFASZbfPLE0jiIX', 'mhw1sria5etp85mdndmvmohp1ql1ym6i', 'xz3V34MN7lXqf9Mdp9aro6dq5Cx3ChLRvDlevSLR6hP', 1, 'https://api.iwindplus.com/api/setup/wechat/getWechatMpQrCodeCallback', 'https://www.iwindplus.com/bindUser', 0, 1543953063620931586);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
