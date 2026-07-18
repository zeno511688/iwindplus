/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码返回值枚举.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
@Getter
public enum MgtCodeEnum implements CommonException {
    /**
     * 子集未删除.
     */
    CHILDREN_NOT_DELETED("children_not_deleted", "子集未删除"),

    /**
     * 资源未删除.
     */
    RESOURCE_NOT_DELETED("resource_not_deleted", "资源未删除"),

    /**
     * 编码已经存在.
     */
    CODE_EXIST("code_exist", "编码已经存在"),

    /**
     * 路由ID已经存在.
     */
    ROUTE_ID_EXIST("route_id_exist", "路由ID已经存在"),

    /**
     * 名称已经存在.
     */
    NAME_EXIST("name_exist", "名称已经存在"),

    /**
     * 手机不能为空.
     */
    MOBILE_NOT_EMPTY("mobile_not_empty", "手机不能为空"),

    /**
     * 手机已经存在 .
     */
    MOBILE_EXIST("mobile_exist", "手机已经存在"),

    /**
     * 手机不存在.
     */
    MOBILE_NOT_EXIST("mobile_not_exist", "手机不存在"),

    /**
     * 邮箱不能为空.
     */
    MAIL_NOT_EMPTY("mail_not_empty", "邮箱不能为空"),

    /**
     * 邮箱未被绑定.
     */
    MAIL_NOT_EXIST("mail_not_exist", "邮箱未被绑定"),

    /**
     * 邮箱已经被绑定了.
     */
    MAIL_EXIST("mail_exist", "邮箱已经被绑定了"),

    /**
     * 工号已经存在.
     */
    JOB_NUMBER_EXIST("job_number_exist", "工号已经存在"),

    /**
     * 用户名已经存在.
     */
    USERNAME_EXIST("username_exist", "用户名已经存在"),

    /**
     * 用户名不支持修改.
     */
    USERNAME_NOT_SUPPORT_MODIFY("USERNAME_NOT_SUPPORT_MODIFY", "用户名不支持修改"),

    /**
     * 身份证已经存在.
     */
    ID_CARD_EXIST("id_card_exist", "身份证已经存在"),

    /**
     * 身份证格式错误.
     */
    ID_CARD_FORMAT_ERROR("id_card_format_error", "身份证格式错误"),

    /**
     * 原密码错误.
     */
    OLD_PASSWORD_ERROR("old_password_error", "原密码错误"),

    /**
     * 新密码不能与原密码相同.
     */
    PASSWORD_COMMON("password_common", "新密码不能与原密码相同"),

    /**
     * 账号不能为空
     */
    ACCOUNT_NOT_EMPTY("account_not_empty", "账号不能为空"),

    /**
     * 密码不能为空.
     */
    PASSWORD_NOT_EMPTY("password_not_empty", "密码不能为空"),

    /**
     * 组织不存在.
     */
    ORG_NOT_EXIST("org_not_exist", "组织不存在"),

    /**
     * 用户不存在.
     */
    USER_NOT_EXIST("user_not_exist", "用户不存在"),

    /**
     * 角色不存在.
     */
    ROLE_NOT_EXIST("role_not_exist", "角色不存在"),

    /**
     * 用户已切换组织.
     */
    USER_HAS_SWITCHED_ORG("user_has_switched_org", "用户已切换组织"),

    /**
     * 客户端被禁用.
     */
    CLIENT_DISABLED("client_disabled", "客户端被禁用"),

    /**
     * 客户端被锁定.
     */
    CLIENT_LOCKED("client_locked", "客户端被锁定"),

    /**
     * 验证码不能为空.
     */
    CAPTCHA_NOT_EMPTY("captcha_not_empty", "验证码不能为空"),

    /**
     * 编码不能为空.
     */
    CODE_NOT_EMPTY("code_not_empty", "编码不能为空"),

    /**
     * 父级未审核.
     */
    PARENT_UN_AUDITED("parent_un_audited", "父级未审核"),

    /**
     * 只有新建或已驳回状态才能编辑.
     */
    NEW_AND_REJECTED_CAN_EDIT("new_and_rejected_can_edit", "只有新建或已驳回状态才能编辑"),

    /**
     * ️已审核过了.
     */
    AUDITED("audited", "已审核过了"),

    /**
     * 服务断言已经存在.
     */
    SERVER_PREDICATE_EXIST("server_predicate_exist", "服务断言已经存在"),

    /**
     * 路径已经存在.
     */
    URL_EXIST("url_exist", "路径已经存在"),

    /**
     * url前缀与服务访问前缀不匹配.
     */
    URL_PREFIX_ERROR("url_prefix_error", "url前缀与服务访问前缀不匹配"),

    /**
     * mq未配置（交换机、队列、路由Key等）.
     */
    MQ_NO_CONFIG("mq_no_config", "mq未配置（交换机、队列、路由Key等）"),

    /**
     * IP已经存在.
     */
    IP_EXIST("ip_exist", "IP已经存在"),

    /**
     * 角色编码开头应为role.
     */
    ROLE_PREFIX_ERROR("role_prefix_error", "角色编码开头应为role"),

    /**
     * 菜单编码开头应为menu.
     */
    MENU_PREFIX_ERROR("menu_prefix_error", "菜单编码开头应为menu"),

    /**
     * 按钮编码开头应为button.
     */
    BUTTON_PREFIX_ERROR("button_prefix_error", "按钮编码开头应为button"),

    /**
     * API编码开头应为api.
     */
    API_PREFIX_ERROR("api_prefix_error", "API编码开头应为api"),

    /**
     * 路由路径已经存在.
     */
    ROUTE_URL_EXIST("route_url_exist", "路由路径已经存在"),

    /**
     * API路径已经存在.
     */
    API_URL_EXIST("api_url_exist", "API路径已经存在"),

    /**
     * API路径不能为空.
     */
    API_URL_NOT_EMPTY("api_url_not_empty", "API路径不能为空"),

    /**
     * 控制器名称已经存在.
     */
    CONTROLLER_NAME_EXIST("controller_name_exist", "控制器名称已经存在"),

    /**
     * API名称已经存在.
     */
    API_NAME_EXIST("api_name_exist", "API名称已经存在"),

    /**
     * 职位未删除.
     */
    POSITION_NOT_DELETED("position_not_deleted", "职位未删除"),

    /**
     * i18n文件后缀错误.
     */
    I18N_FILE_SUFFIX_ERROR("i18n_file_suffix_error", "i18n文件后缀错误，必须为properties"),

    /**
     * 国际化消息集合中含有长度超过100的值.
     */
    I18N_MSG_VALUE_TOO_LONG("i18n_msg_value_too_long", "国际化消息集合中含有长度超过200的值"),

    /**
     * yubikey配置已经存在.
     */
    YUBIKEY_CONFIG_EXIST("yubikey_config_exist", "yubikey配置已经存在"),

    /**
     * 应用凭证类型已经存在.
     */
    APP_CERT_TYPE_EXIST("app_cert_type_exist", "应用凭证类型已经存在");

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
    MgtCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
