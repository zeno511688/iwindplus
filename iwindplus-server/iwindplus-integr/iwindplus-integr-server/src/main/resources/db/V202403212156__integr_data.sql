SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Records of mail_tpl
-- ----------------------------
BEGIN;
INSERT INTO `mail_tpl` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `code`, `name`, `template_content`, `captcha_timeout`, `captcha_length`, `limit_count_day`, `limit_count_hour`, `limit_count_minute`, `build_in_flag`, `org_id`) VALUES (1543959629879328770, 1757785403815, 'system', 1543960516026822658, 1757785403815, 'system', 1543960516026822658, 0, 7, '邮件验证码', 1, 'c3f67fd355dd6098156053f68285ba3e', '邮件验证码', '验证码： ${captcha}，${timeout}分钟内有效，误泄露给他人，如非本人操作请忽略。', 10, 6, 10, 5, 1, 0, 1543953063620931586);
COMMIT;

-- ----------------------------
-- Records of oss_tpl
-- ----------------------------
BEGIN;
INSERT INTO `oss_tpl` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `bucket_name`, `access_domain`, `build_in_flag`, `org_id`) VALUES (1543959629879323572, 1757785403815, 'system', 1543960516026822658, 1757785403815, 'system', 1543960516026822658, 0, 7, '管理服务上传空间名', 1, 1, 'c3f67fd355dd6098156053f68385ba32', '阿里云管理服务空间名', 'iwindplus-mgt', 'https://iwindplus-mgt.oss-cn-shenzhen.aliyuncs.com', 0, 1543953063620931586);
INSERT INTO `oss_tpl` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `bucket_name`, `access_domain`, `build_in_flag`, `org_id`) VALUES (1543959629879323573, 1757785403815, 'system', 1543960516026822658, 1757785403815, 'system', 1543960516026822658, 0, 11, '即时通讯上传空间名', 1, 1, 'c3f67fd355dd6098156053f68385ba33', '阿里云即时通讯空间名', 'iwindplus-im', 'https://iwindplus-im.oss-cn-shenzhen.aliyuncs.com', 0, 1543953063620931586);
INSERT INTO `oss_tpl` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `bucket_name`, `access_domain`, `build_in_flag`, `org_id`) VALUES (1543959629879323574, 1757785403815, 'system', 1543960516026822658, 1757785403815, 'system', 1543960516026822658, 0, 7, '管理服务上传空间名', 1, 0, 'c3f67fd355dd6098156053f68385ba34', 'minio管理服务空间名', 'iwindplus-mgt', '', 0, 1543953063620931586);
INSERT INTO `oss_tpl` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `bucket_name`, `access_domain`, `build_in_flag`, `org_id`) VALUES (1543959629879323575, 1757785403815, 'system', 1543960516026822658, 1757785403815, 'system', 1543960516026822658, 0, 7, '即时通讯服务上传空间名', 1, 0, 'c3f67fd355dd6098156053f68385ba35', 'minio即时通讯空间名', 'iwindplus-im', '', 1, 1543953063620931586);
COMMIT;


-- ----------------------------
-- Records of sms_tpl
-- ----------------------------
BEGIN;
INSERT INTO `sms_tpl` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `sign_name`, `template_content`, `captcha_timeout`, `captcha_length`, `limit_count_day`, `limit_count_hour`, `limit_count_minute`, `build_in_flag`, `org_id`) VALUES (1543959629879324283, 1757785403815, 'system', 1543960516026822658, 1757785403815, 'system', 1543960516026822658, 0, 7, '验证码（验证码）', 1, 0, 'c3f67fd354dd6098154053f68285ba35', '阿里云-验证码（验证码）', 'iwindplus', 'SMS_232891215', 10, 6, 10, 5, 1, 0, 1543953063620931586);
INSERT INTO `sms_tpl` (`id`, `created_timestamp`, `created_by`, `created_id`, `modified_timestamp`, `modified_by`, `modified_id`, `deleted`, `version`, `remark`, `status`, `type`, `code`, `name`, `sign_name`, `template_content`, `captcha_timeout`, `captcha_length`, `limit_count_day`, `limit_count_hour`, `limit_count_minute`, `build_in_flag`, `org_id`) VALUES (1543959629879324284, 1757785403815, 'system', 1543960516026822658, 1757785403815, 'system', 1543960516026822658, 0, 10, '敏感操作（验证码）', 1, 0, 'c3f67fd354dd6098154053f68285ba35', '阿里云-敏感操作（验证码）', 'iwindplus', 'SMS_232891230', 10, 6, 10, 5, 1, 0, 1543953063620931586);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
