package com.iwindplus.setup.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码枚举.
 *
 * @author zengdegui
 * @since 2024/04/30 14:47
 */
@Getter
public enum SetupCodeEnum implements CommonException {
    /**
     * 邮箱配置编码不能为空.
     */
    MAIL_CONFIG_CODE_NOT_EMPTY("mail_config_code_not_empty", "邮箱配置编码不能为空"),

    /**
     * 邮箱配置不存在.
     */
    MAIL_CONFIG_NOT_EXIST("mail_config_not_exist", "邮箱配置不存在"),

    /**
     * 邮箱配置被禁用.
     */
    MAIL_CONFIG_DISABLED("mail_config_disabled", "邮箱配置被禁用"),

    /**
     * 邮箱配置被锁定.
     */
    MAIL_CONFIG_LOCKED("mail_config_locked", "邮箱配置被锁定"),

    /**
     * 邮箱模板不存在.
     */
    MAIL_TEMPLATE_NOT_EXIST("mail_template_not_exist", "邮箱模板不存在"),

    /**
     * 邮箱模板被禁用.
     */
    MAIL_TEMPLATE_DISABLED("mail_template_disabled", "邮箱模板被禁用"),

    /**
     * 邮箱模板被锁定.
     */
    MAIL_TEMPLATE_LOCKED("mail_template_locked", "邮箱模板被锁定"),

    /**
     * 邮箱模板参数不能为空.
     */
    MAIL_TEMPLATE_PARAM_NOT_EMPTY("mail_template_param_not_empty", "邮箱模板参数不能为空"),

    /**
     * 邮箱模板参数有误.
     */
    MAIL_TEMPLATE_PARAM_ERROR("mail_template_param_error", "邮箱模板参数有误"),

    /**
     * 邮箱主题不能为空.
     */
    MAIL_SUBJECT_NOT_EMPTY("mail_subject_not_empty", "邮箱主题不能为空"),

    /**
     * 邮箱内容不能为空.
     */
    MAIL_CONTENT_NOT_EMPTY("mail_content_not_empty", "邮箱内容不能为空"),

    /**
     * 发件服务器账户已经存在.
     */
    SMTP_USERNAME_EXIST("smtp_username_exist", "发件服务器账户已经存在"),

    /**
     * 对象存储配置不存在.
     */
    OSS_CONFIG_NOT_EXIST("oss_config_not_exist", "对象存储配置不存在"),

    /**
     * 对象存储配置被禁用.
     */
    OSS_CONFIG_DISABLED("oss_config_disabled", "对象存储配置被禁用"),

    /**
     * 对象存储配置被锁定.
     */
    OSS_CONFIG_LOCKED("oss_config_locked", "对象存储配置被锁定"),

    /**
     * 对象存储模板不存在.
     */
    OSS_TEMPLATE_NOT_EXIST("oss_template_not_exist", "对象存储模板不存在"),

    /**
     * 对象存储模板被禁用.
     */
    OSS_TEMPLATE_DISABLED("oss_template_disabled", "对象存储模板被禁用"),

    /**
     * 对象存储模板被锁定.
     */
    OSS_TEMPLATE_LOCKED("oss_template_locked", "对象存储模板被锁定"),

    /**
     * 空间名已经存在.
     */
    BUCKET_NAME_EXIST("bucket_name_exist", "空间名已经存在"),

    /**
     * 短信配置主键不能为空.
     */
    SMS_CONFIG_ID_NOT_EMPTY("sms_config_id_not_empty", "短信配置主键不能为空"),

    /**
     * 短信配置不存在.
     */
    SMS_CONFIG_NOT_EXIST("sms_config_not_exist", "短信配置不存在"),

    /**
     * 短信配置被禁用.
     */
    SMS_CONFIG_DISABLED("sms_config_disabled", "短信配置被禁用"),

    /**
     * 短信配置被锁定.
     */
    SMS_CONFIG_LOCKED("sms_config_locked", "短信配置被锁定"),

    /**
     * 短信模板不存在.
     */
    SMS_TEMPLATE_NOT_EXIST("sms_template_not_exist", "短信模板不存在"),

    /**
     * 短信模板被禁用.
     */
    SMS_TEMPLATE_DISABLED("sms_template_disabled", "短信模板被禁用"),

    /**
     * 短信模板被锁定.
     */
    SMS_TEMPLATE_LOCKED("sms_template_locked", "短信模板被锁定"),

    /**
     * 短信模板code已经存在.
     */
    SMS_TEMPLATE_CODE_EXIST("sms_template_code_exist", "短信模板code已经存在"),

    /**
     * 服务器区域已经存在.
     */
    REGION_EXIST("region_exist", "服务器区域已经存在"),

    /**
     * 视频点播配置不存在.
     */
    VOD_CONFIG_NOT_EXIST("vod_config_not_exist", "视频点播配置不存在"),

    /**
     * 视频点播配置被禁用.
     */
    VOD_CONFIG_DISABLED("vod_config_disabled", "视频点播配置被禁用"),

    /**
     * 视频点播配置被锁定.
     */
    VOD_CONFIG_LOCKED("vod_config_locked", "视频点播配置被锁定"),

    /**
     * 微信小程序appId已经存在.
     */
    WECHAT_CONFIG_MA_ACCESS_KEY_EXIST("wechat_config_ma_access_key_exist", "微信小程序appId已经存在"),

    /**
     * 微信小程序配置不存在.
     */
    WECHAT_CONFIG_MA_NOT_EXIST("wechat_config_ma_not_exist", "微信小程序配置不存在"),

    /**
     * 微信小程序配置被禁用.
     */
    WECHAT_CONFIG_MA_DISABLED("wechat_config_ma_disabled", "微信小程序配置被禁用"),

    /**
     * 微信小程序配置被锁定.
     */
    WECHAT_CONFIG_MA_LOCKED("wechat_config_ma_locked", "微信小程序配置被锁定"),

    /**
     * 微信公众号appId已经存在.
     */
    WECHAT_CONFIG_MP_ACCESS_KEY_EXIST("wechat_config_mp_access_key_exist", "微信公众号appId已经存在"),

    /**
     * 微信小公众号配置不存在.
     */
    WECHAT_CONFIG_MP_NOT_EXIST("wechat_config_ma_not_exist", "微信公众号配置不存在"),

    /**
     * 微信公众号配置被禁用.
     */
    WECHAT_CONFIG_MP_DISABLED("wechat_config_ma_disabled", "微信公众号配置被禁用"),

    /**
     * 微信公众号配置被锁定.
     */
    WECHAT_CONFIG_MP_LOCKED("wechat_config_ma_locked", "微信公众号配置被锁定"),

    /**
     * 短信配置编码不能为空.
     */
    SMS_CONFIG_CODE_NOT_EMPTY("sms_config_code_not_empty", "短信配置编码不能为空"),

    /**
     * 名称在表中重复.
     */
    NAME_REPEAT_IN_TABLE("name_repeat_in_table", "名称在表中重复"),

    /**
     * 发件服务器端口不是整数.
     */
    SMTP_PORT_NOT_INTEGER("smtp_port_not_integer", "发件服务器端口不是整数"),

    /**
     * 发件服务器账户在表中重复.
     */
    SMTP_USERNAME_REPEAT_IN_TABLE("smtp_username_repeat_in_table", "发件服务器账户在表中重复"),

    /**
     * 回调地址格式不正确.
     */
    NOTIFY_URL_FORMAT_ERROR("notify_url_format_error", "回调地址格式不正确"),

    /**
     * 回调成功地址不正确.
     */
    NOTIFY_SUCCESS_URL_FORMAT_ERROR("notify_success_url_format_error", "回调成功地址不正确"),

    /**
     * 公众号appId在表中重复.
     */
    MP_APP_ID_REPEAT_IN_TABLE("mp_app_id_repeat_in_table", "公众号appId在表中重复"),

    /**
     * 小程序appId在表中重复.
     */
    MA_APP_ID_REPEAT_IN_TABLE("ma_app_id_repeat_in_table", "小程序appId在表中重复"),

    /**
     * 服务器区域在表中重复.
     */
    SERVER_REGION_ID_REPEAT_IN_TABLE("server_region_repeat_in_table", "服务器区域在表中重复"),

    /**
     * 服务器区域已存在.
     */
    SERVER_REGION_EXIST("server_region_exist", "服务器区域已存在"),

    ;

    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务信息.
     */
    private final String bizMessage;

    /**
     * 构造方法.
     *
     * @param bizCode    业务编码
     * @param bizMessage 业务信息
     */
    SetupCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
