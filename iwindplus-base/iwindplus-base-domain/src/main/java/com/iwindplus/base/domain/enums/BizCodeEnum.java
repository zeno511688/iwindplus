/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import java.util.stream.Stream;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/**
 * 业务编码枚举.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Getter
@RequiredArgsConstructor
public enum BizCodeEnum implements CommonException {

    /**
     * 请求ID不能为空.
     */
    REQUEST_ID_NOT_EMPTY("request_id_not_empty", "请求ID不能为空"),

    /**
     * 空响应.
     */
    EMPTY_RESPONSE("empty_response", "空响应"),

    /**
     * 空响应体.
     */
    EMPTY_RESPONSE_BODY("empty_response_body", "空响应体"),

    /**
     * 响应不是json.
     */
    RESPONSE_NOT_JSON("response_not_json", "响应不是json"),

    /**
     * 执行超时.
     */
    EXECUTE_TIMEOUT("execute_timeout", "执行超时"),

    /**
     * 执行错误.
     */
    EXECUTE_ERROR("execute_error", "执行错误"),

    /**
     * 系统繁忙，请稍后重试.
     */
    SYSTEM_BUSY("system_busy", "系统繁忙，请稍后重试"),

    /**
     * 禁止访问.
     */
    FORBIDDEN_ACCESS("forbidden_access", "禁止访问"),

    /**
     * 客户端错误.
     */
    CLIENT_ERROR("client_error", "客户端错误"),

    /**
     * 服务端错误.
     */
    SERVER_ERROR("server_error", "服务端错误"),

    /**
     * 系统错误.
     */
    SYSTEM_ERROR("system_error", "系统错误"),

    /**
     * 未知主机.
     */
    UNKNOWN_HOST("unknown_host", "未知主机"),

    /**
     * 网络连接错误.
     */
    SOCKET_ERROR("socket_error", "网络连接错误"),

    /**
     * 网络连接错误.
     */
    SERVICE_UNAVAILABLE("service_unavailable", "服务不可用"),

    /**
     * 不可读.
     */
    NOT_READABLE("not_readable", "不可读，请检查参数格式是否正确"),

    /**
     * 不可写.
     */
    NOT_WRITABLE("not_writable", "不可写"),

    /**
     * 空指针异常.
     */
    NULL_POINTER("null_pointer", "空指针异常"),

    /**
     * 转换不支持.
     */
    CONVERSION_NOT_SUPPORTED("conversion_not_supported", "转换不支持"),

    /**
     * 类型转换错误.
     */
    CLASS_CAST_ERROR("class_cast_error", "类型转换错误"),

    /**
     * 只支持数字.
     */
    ONLY_SUPPORT_NUMBER("only_support_number", "只支持数字"),

    /**
     * 安全错误.
     */
    SECURITY_ERROR("security_error", "安全错误"),

    /**
     * sql语法错误.
     */
    BAD_SQL_GRAMMAR("bad_sql_grammar", "sql语法错误"),

    /**
     * sql错误.
     */
    SQL_ERROR("sql_error", "sql错误"),

    /**
     * mybatis错误.
     */
    MYBATIS_ERROR("mybatis_error", "mybatis错误"),

    /**
     * 数据完整性违规（update或insert）.
     */
    DATA_INTEGRITY_VIOLATION_ERROR("data_integrity_violation_error", "数据完整性违规"),

    /**
     * 类型不存在.
     */
    TYPE_NOT_PRESENT("type_not_present", "类型不存在"),

    /**
     * IO错误.
     */
    IO_ERROR("io_error", "IO错误"),

    /**
     * 未知方法.
     */
    NO_SUCH_METHOD("no_such_method", "未知方法"),

    /**
     * 未知状态.
     */
    UNKNOWN_STATUS("unknown_status", "未知状态"),

    /**
     * 数组越界.
     */
    INDEX_OUT_OF_BOUNDS("index_out_of_bounds", "数组越界"),

    /**
     * 无法注入bean.
     */
    NO_SUCH_BEAN("no_such_bean", "无法注入bean"),

    /**
     * 请检查bean是否注入.
     */
    NOT_FOUND_BEAN("not_found_bean", "请检查bean是否注入"),

    /**
     * 类型不匹配.
     */
    TYPE_MISMATCH("type_mismatch", "类型不匹配"),

    /**
     * 栈溢出.
     */
    STACK_OVERFLOW("stack_overflow", "栈溢出"),

    /**
     * 算数错误.
     */
    ARITHMETIC_ERROR("arithmetic_error", "算数错误"),

    /**
     * 参数错误.
     */
    PARAM_ERROR("param_error", "参数错误"),

    /**
     * 参数缺失.
     */
    PARAM_MISS("param_miss", "参数（{0}）缺失"),

    /**
     * 非法参数.
     */
    PARAM_ILLEGAL("param_illegal", "非法参数"),

    /**
     * 参数类型不匹配.
     */
    PARAM_TYPE_MISMATCH("param_type_mismatch", "参数（{0}）类型不匹配"),

    /**
     * 参数违反约束.
     */
    PARAM_CONSTRAINT_VIOLATION("param_constraint_violation", "参数违反约束"),

    /**
     * 参数无效.
     */
    PARAM_INVALID("param_invalid", "参数无效"),

    /**
     * 参数绑定错误.
     */
    PARAM_BIND_ERROR("param_bind_error", "参数绑定错误"),

    /**
     * 参数输入错误.
     */
    PARAM_INPUT_ERROR("param_input_error", "参数（{0}）输入错误"),

    /**
     * json映射错误.
     */
    JSON_MAPPING_ERROR("json_mapping_error", "json映射错误"),

    /**
     * json处理错误.
     */
    JSON_PROCESSING_ERROR("json_processing_error", "json处理错误"),

    /**
     * 解析错误.
     */
    PARSE_ERROR("parse_error", "解析错误"),

    /**
     * 序列化错误.
     */
    SERIALIZE_ERROR("serialize_error", "序列化错误"),

    /**
     * 反序列化错误.
     */
    DESERIALIZE_ERROR("deserialize_error", "反序列化错误"),

    /**
     * 请求超时.
     */
    REQUEST_TIMEOUT("request_timeout", "请求超时"),

    /**
     * rpc调用异常.
     */
    RPC_ERROR("rpc_error", "rpc调用异常"),

    /**
     * 发送错误，请检查配置是否正确.
     */
    SEND_ERROR("send_error", "发送错误，请检查配置是否正确"),

    /**
     * 不支持的操作.
     */
    UNSUPPORTED_OPERATION("unsupported_operation", "不支持的操作"),

    /**
     * 操作错误.
     */
    OPERATE_ERROR("operate_error", "操作错误"),

    /**
     * 应用凭证配置不存在.
     */
    APP_CERT_CONFIG_NOT_EXIST("app_cert_config_not_exist", "应用凭证配置不存在"),

    /**
     * 访问key不存在.
     */
    ACCESS_KEY_NOT_EXIST("access_key_not_exist", "访问key不存在"),

    /**
     * 访问key已存在.
     */
    ACCESS_KEY_EXIST("access_key_exist", "访问key已存在"),

    /**
     * 密钥不存在.
     */
    SECRET_KEY_NOT_EXIST("secret_key_not_exist", "密钥不存在"),

    /**
     * 密钥已存在.
     */
    SECRET_KEY_EXIST("secret_key_exist", "密钥已存在"),

    /**
     * 签名不存在.
     */
    SIGN_NOT_EXIST("sign_not_exist", "签名不存在"),

    /**
     * 签名已存在.
     */
    SIGN_EXIST("sign_exist", "签名已存在"),

    /**
     * 时间戳不存在.
     */
    TIMESTAMP_NOT_EXIST("timestamp_not_exist", "时间戳不存在"),

    /**
     * 请求路径不存在.
     */
    REQUEST_PATH_NOT_EXIST("request_path_not_exist", "请求路径不存在"),

    /**
     * 请求路径已存在.
     */
    REQUEST_PATH_EXIST("request_path_exist", "请求路径已存在"),

    /**
     * 请求方式不存在.
     */
    REQUEST_METHOD_NOT_EXIST("request_method_not_exist", "请求方式不存在"),

    /**
     * 请求方式已存在.
     */
    REQUEST_METHOD_EXIST("request_method_exist", "请求方式已存在"),

    /**
     * 签名超时时间不存在.
     */
    SIGN_TIMEOUT_NOT_EXIST("sign_timeout_not_exist", "签名超时时间不存在"),

    /**
     * 签名过期.
     */
    SIGN_EXPIRED("sign_expired", "签名过期，有效时间为{0}秒"),

    /**
     * 无效随机数.
     */
    INVALID_NONCE("invalid_nonce", "无效随机数，长度必须大于10"),

    /**
     * 无效签名.
     */
    INVALID_SIGN("invalid_sign", "无效签名"),

    /**
     * 不支持的线程拒绝策略.
     */
    UNSUPPORTED_THREAD_REJECTED_EXECUTION("unsupported_thread_rejected_execution", "不支持的线程拒绝策略（{0}）"),

    /**
     * 解码错误.
     */
    DECODING_ERROR("decoding_error", "解码错误"),

    /**
     * 批量限制错误.
     */
    BATCH_LIMIT_ERROR("batch_limit_error", "批量限制错误，不能超过（{0}）"),

    /**
     * 业务流水号已经存在.
     */
    BIZ_NUMBER_EXIST("biz_number_exist", "业务流水号已经存在（{0}）"),

    /**
     * 加密盐不存在.
     */
    SALT_NOT_EXIST("salt_not_exist", "加密盐不存在"),

    /**
     * 数据库名称不存在.
     */
    DB_NAME_NOT_EXIST("db_name_not_exist", "数据库名称不存在"),

    /**
     * 表名不存在.
     */
    TABLE_NAME_NOT_EXIST("table_name_not_exist", "表名不存在"),

    /**
     * 操作不存在.
     */
    ACTION_NOT_EXIST("action_not_exist", "操作不存在"),

    /**
     * url转pdf字节错误.
     */
    URL_TO_PDF_BYTES_ERROR("url_to_pdf_bytes_error", "url转pdf字节错误"),

    /**
     * url转pdf下载错误.
     */
    URL_TO_PDF_DOWNLOAD_ERROR("url_to_pdf_download_error", "url转pdf下载错误"),

    /**
     * html转pdf字节错误.
     */
    HTML_TO_PDF_BYTES_ERROR("html_to_pdf_bytes_error", "html转pdf字节错误"),

    /**
     * html转pdf下载错误.
     */
    HTML_TO_PDF_DOWNLOAD_ERROR("html_to_pdf_download_error", "html转pdf下载错误"),

    /**
     * 无效的策略.
     */
    INVALID_STRATEGY("invalid_strategy", "无效策略"),

    /**
     * 批量操作个数太大.
     */
    BATCH_OPERATION_QUANTITY_TOO_BIG("batch_operation_quantity_too_big", "批量操作个数太大，不能超过{0}"),

    // --------------------------------------------------------------------------------------

    /**
     * 业务处理中，请稍后再试.
     */
    BUSINESS_PROCESS("business_process", "业务处理中，请稍后再试"),

    /**
     * 重复提交.
     */
    REPEAT_SUBMIT("repeat_submit", "重复提交，请等待{0}秒后再操作"),

    /**
     * 执行超时.
     */
    IDEMPOTENT_EXECUTE_TIMEOUT("idempotent_execute_timeout", "执行超时"),

    /**
     * 请求状态无效.
     */
    IDEMPOTENT_REQUEST_INVALID("idempotent_request_invalid", "请求状态无效"),

    /**
     * 重复请求.
     */
    IDEMPOTENT_REQUEST_DUPLICATE("idempotent_request_duplicate", "重复请求"),

    /**
     * 业务已处理.
     */
    IDEMPOTENT_BIZ_DUPLICATE("idempotent_biz_duplicate", "业务已处理"),

    /**
     * 业务正在处理，请等待.
     */
    IDEMPOTENT_EXECUTE_PLEASE_WAIT("idempotent_execute_please_wait", "业务正在处理，请等待{0}秒后再操作"),

    /**
     * 已审核了.
     */
    ALREADY_AUDITED("already_audited", "已审核了"),

    /**
     * 已操作了.
     */
    ALREADY_OPERATED("already_operated", "已操作了"),

    /**
     * 含有内置数据.
     */
    HAS_BUILD_IN_DATA("has_build_in_data", "含有内置数据"),

    /**
     * 内置数据不能操作.
     */
    BUILD_IN_DATA_NOT_OPERATE("build_in_data_not_operate", "内置数据不能操作"),

    /**
     * IP为黑名单.
     */
    IP_IS_BLACKLISTED("ip_is_blacklisted", "IP（{0}）为黑名单"),

    /**
     * API未注册.
     */
    API_UNREGISTERED("api_unregistered", "API未注册"),

    /**
     * 无API权限.
     */
    NO_API_PERMISSION("no_api_permission", "无API权限"),

    /**
     * 数据已经存在.
     */
    DATA_EXIST("data_exist", "数据已经存在"),

    /**
     * 数据不存在.
     */
    DATA_NOT_EXIST("data_not_exist", "数据不存在"),

    /**
     * 编码不存在.
     */
    CODE_NOT_EXIST("code_not_exist", "编码不存在"),

    /**
     * 编码只能使用一次.
     */
    CODE_CAN_USE_ONCE("code_can_use_once", "编码只能使用一次"),

    /**
     * 无效编码.
     */
    INVALID_CODE("invalid_code", "无效编码"),

    /**
     * 账号不存在.
     */
    ACCOUNT_NOT_EXIST("account_not_exist", "账号不存在"),

    /**
     * 账号被锁定.
     */
    ACCOUNT_LOCKED("account_locked", "账号被锁定"),

    /**
     * 账号被禁用.
     */
    ACCOUNT_DISABLED("account_disabled", "账号被禁用"),

    /**
     * 账号过期.
     */
    ACCOUNT_EXPIRED("account_expired", "账号过期"),

    /**
     * 密码错误.
     */
    PASSWORD_ERROR("password_error", "密码错误"),

    /**
     * 用户密码已过期.
     */
    PASSWORD_EXPIRED("password_expired", "用户密码已过期"),

    /**
     * 用户名或密码错误.
     */
    USERNAME_PASSWORD_ERROR("username_password_error", "用户名或密码错误"),

    /**
     * 用户未登陆.
     */
    USER_NOT_LOGIN("user_not_login", "用户未登陆"),

    /**
     * 凭证（密码）过期.
     */
    CREDENTIALS_EXPIRED("credentials_expired", "凭证（密码）过期"),

    /**
     * 错误凭证.
     */
    BAD_CREDENTIALS("bad_credentials", "错误凭证"),

    /**
     * token不存在.
     */
    TOKEN_NOT_EXIST("token_not_exist", "token不存在"),

    /**
     * 无效访问token.
     */
    INVALID_ACCESS_TOKEN("invalid_access_token", "无效访问token"),

    /**
     * 访问token未过期.
     */
    ACCESS_TOKEN_NOT_EXPIRED("access_token_not_expired", "访问token未过期"),

    /**
     * 无效刷新token.
     */
    INVALID_REFRESH_TOKEN("invalid_refresh_token", "无效刷新token"),

    /**
     * 手机不存在.
     */
    MOBILE_NOT_EXIST("mobile_not_exist", "手机不存在"),

    /**
     * 手机号码格式错误.
     */
    MOBILE_FORMAT_ERROR("mobile_format_error", "手机号码格式错误"),

    /**
     * 频率限制.
     */
    FREQUENCY_LIMIT("frequency_limit", "频率限制"),

    /**
     * 扫描错误.
     */
    SCAN_ERROR("scan_error", "扫描错误"),

    /**
     * 配置不能为空.
     */
    CONFIG_NOT_EMPTY("config_not_empty", "配置不能为空"),

    /**
     * 配置错误.
     */
    CONFIG_ERROR("config_error", "配置错误"),

    /**
     * 配置未找到.
     */
    CONFIG_NOT_FOUND("config_not_found", "未找到对应的配置（{0}）"),

    /**
     * 获取缓存错误.
     */
    GET_BUFFER_ERROR("get_buffer_error", "获取缓存错误"),

    /**
     * 命令类型已经存在.
     */
    CMD_TYPE_EXIST("cmd_type_exist", "命令类型已经存在（{0}）"),

    /**
     * 任务类型已经存在.
     */
    TASK_TYPE_EXIST("task_type_exist", "任务类型已经存在（{0}）"),

    /**
     * 执行器名称已经存在.
     */
    EXECUTE_NAME_EXIST("execute_name_exist", "执行器名称已经存在（{0}）"),

    // --------------------------------------------------------------------------------------
    /**
     * 重复执行，请稍后再试.
     */
    REPEAT_EXECUTE("repeat_execute", "重复执行，请稍后再试"),

    /**
     * 请求过快，请稍后重试.
     */
    REQUEST_TOO_FAST("request_too_fast", "请求过快，请稍后重试"),

    /**
     * 重试耗尽.
     */
    RETRY_EXHAUSTED("retry_exhausted", "重试耗尽"),

    /**
     * 不支持的类型.
     */
    UNSUPPORTED_TYPE("unsupported_type", "不支持的类型（{0}）"),

    /**
     * 获取锁异常.
     */
    GET_LOCK_ERROR("get_lock_error", "获取锁异常，lock={0}"),

    /**
     * 生成序列号错误.
     */
    GENERATE_SERIAL_NUM_ERROR("generate_serial_num_error", "生成序列号错误，lock={0}"),

    /**
     * 限流器初始化错误.
     */
    RATE_LIMITER_INIT_ERROR("rate_limiter_init_error", "限流器初始化错误，name={0}"),

    // --------------------------------------------------------------------------------------

    /**
     * GA二维码生成错误.
     */
    GA_QRCODE_GENERATE_ERROR("ga_qrcode_generate_error", "GA二维码生成错误"),

    /**
     * ga未绑定.
     */
    GA_UNBOUND("ga_unbound", "ga未绑定"),

    /**
     * ga已经绑定.
     */
    GA_ALREADY_BOUND("ga_already_bound", "ga已经绑定"),

    /**
     * ga已经重置.
     */
    GA_ALREADY_RESET("ga_already_reset", "ga已经重置"),

    /**
     * ga验证码不能为空.
     */
    GA_CAPTCHA_NOT_EMPTY("ga_captcha_not_empty", "ga验证码不能为空"),

    /**
     * ga验证码错误.
     */
    GA_CAPTCHA_ERROR("ga_captcha_error", "ga验证码错误"),

    /**
     * 邮箱未绑定.
     */
    MAIL_UNBOUND("mail_unbound", "邮箱未绑定"),

    /**
     * 邮箱验证码不能为空.
     */
    MAIL_CAPTCHA_NOT_EMPTY("mail_captcha_not_empty", "邮箱验证码不能为空"),

    /**
     * 邮箱验证码错误.
     */
    MAIL_CAPTCHA_ERROR("mail_captcha_error", "邮箱验证码错误"),

    /**
     * 手机未绑定.
     */
    MOBILE_UNBOUND("mobile_unbound", "手机未绑定"),

    /**
     * yubikey未绑定.
     */
    YUBIKEY_UNBOUND("yubikey_unbound", "yubikey未绑定"),

    /**
     * 短信验证码不能为空.
     */
    SMS_CAPTCHA_NOT_EMPTY("sms_captcha_not_empty", "短信验证码不能为空"),

    /**
     * 短信验证码错误.
     */
    SMS_CAPTCHA_ERROR("sms_captcha_error", "短信验证码错误"),

    /**
     * yubikey原数据不能为空.
     */
    YUBIKEY_SOURCE_NOT_EMPTY("yubikey_source_not_empty", "yubikey原数据不能为空"),

    /**
     * yubikey签名不能为空.
     */
    YUBIKEY_SIGN_NOT_EMPTY("yubikey_sign_not_empty", "yubikey签名不能为空"),

    /**
     * yubikey校验错误.
     */
    YUBIKEY_VERIFY_ERROR("yubikey_verify_error", "yubikey校验错误"),

    // --------------------------------------------------------------------------------------

    /**
     * 文件名不存在.
     */
    FILE_NAME_NOT_EXIST("file_name_not_exist", "文件名不存在"),

    /**
     * 文件名存在.
     */
    FILE_NAME_EXIST("file_name_exist", "文件名存在"),

    /**
     * 文件找不到.
     */
    FILE_NOT_FOUND("file_not_found", "文件找不到"),

    /**
     * 所需的请求部分"文件"不存在.
     */
    MISSING_FILE("missing_file", "所需的请求部分文件不存在"),

    /**
     * 文件大小被限制.
     */
    FILE_SIZE_LIMIT("file_size_limit", "文件大小超出服务器限制"),

    /**
     * 文件太大.
     */
    FILE_TOO_BIG("file_too_big", "文件太大，大小为（{0}）"),

    /**
     * 文件分片太大.
     */
    FILE_PART_TOO_BIG("file_part_too_big", "文件分片太大，不能超过（{0}）"),

    /**
     * 文件上传错误.
     */
    FILE_UPLOAD_ERROR("file_upload_error", "文件上传错误"),

    /**
     * 文件下载错误.
     */
    FILE_DOWNLOAD_ERROR("file_download_error", "文件下载错误"),

    /**
     * 文件删除错误.
     */
    FILE_DELETE_ERROR("file_delete_error", "文件删除错误"),

    /**
     * 文件目录为空.
     */
    FILE_DIR_EMPTY("file_dir_empty", "文件目录为空"),

    /**
     * 创建目录错误.
     */
    FILE_DIR_CREATE_ERROR("file_dir_create_error", "创建目录错误"),

    /**
     * 文件不是图片.
     */
    FILE_IS_NOT_IMAGE("file_is_not_image", "文件不是图片，格式为（{0}）"),

    /**
     * 文件没有后缀.
     */
    FILE_HAS_NOT_SUFFIX("file_has_not_suffix", "文件没有后缀"),

    // --------------------------------------------------------------------------------------

    /**
     * 获取临时访问凭证失败.
     */
    GET_ACCESS_CREDENTIALS_ERROR("get_access_credentials_error", "获取临时访问凭证失败"),

    /**
     * 获取签名路径错误.
     */
    GET_SIGN_URL_ERROR("get_sign_url_error", "获取签名路径错误"),

    /**
     * 获取播放凭证失败.
     */
    GET_PLAY_AUTH_FAILED("get_play_auth_failed", "获取播放凭证失败"),

    /**
     * 获取视频信息失败.
     */
    GET_VIDEO_FAILED("get_video_failed", "获取视频信息失败"),

    /**
     * 获取源视频信息失败.
     */
    GET_SOURCE_VIDEO_FAILED("get_source_video_failed", "获取源视频信息失败"),

    /**
     * 删除视频文件失败.
     */
    DELETE_VIDEO_FAILED("delete_video_failed", "删除视频文件失败"),

    /**
     * 提交智能AI审核失败.
     */
    SUBMIT_AI_AUDIT_FAILED("submit_ai_audit_failed", "提交智能AI审核失败"),

    /**
     * 提交人工审核失败.
     */
    SUBMIT_MANUAL_AUDIT_FAILED("submit_manual_audit_failed", "提交人工审核失败"),

    // --------------------------------------------------------------------------------------

    /**
     * 账户余额不足.
     */
    AMOUNT_NOT_ENOUGH("amount_not_enough", "账户余额不足"),

    /**
     * 验证码错误.
     */
    CAPTCHA_ERROR("captcha_error", "验证码错误"),

    /**
     * 验证码过期.
     */
    CAPTCHA_EXPIRED("captcha_expired", "验证码过期"),

    /**
     * 验证码未过期或未使用.
     */
    CAPTCHA_NOT_EXPIRED("captcha_not_expired", "验证码未过期或未使用，请勿重新发送"),

    /**
     * 验证码只能使用一次.
     */
    CAPTCHA_CAN_USE_ONCE("captcha_can_use_once", "验证码只能使用一次"),

    /**
     * 验证码每天发送次数不超过{0}次.
     */
    CAPTCHA_LIMIT_DAY("captcha_limit_day", "验证码每天发送次数不超过{0}次"),

    /**
     * 验证码每小时发送次数不超过{0}次.
     */
    CAPTCHA_LIMIT_HOUR("captcha_limit_hour", "验证码每小时发送次数不超过{0}次"),

    /**
     * 验证码每分钟发送次数不超过{0}次.
     */
    CAPTCHA_LIMIT_MINUTE("captcha_limit_minute", "验证码每分钟发送次数不超过{0}次"),

    /**
     * 模板参数不支持url.
     */
    TEMPLATE_PARAM_NOT_SUPPORT_URL("template_param_not_support_url", "模板参数不支持url"),

    // --------------------------------------------------------------------------------------

    /**
     * 无效的密钥，长度必须为32位（AES）.
     */
    INVALID_AES_KEY("invalid_aes_key", "无效的密钥，长度必须为32位"),

    /**
     * 无效的初始化向量，长度必须为12位.
     */
    INVALID_AES_IV("invalid_aes_iv", "无效的初始化向量，长度必须为12位"),

    /**
     * 无效的密钥，长度必须为16位（SM4）.
     */
    INVALID_SM4_KEY("invalid_sm4_key", "无效的密钥，长度必须为16位"),

    /**
     * 不支持的算法.
     */
    CRYPTO_ALGORITHM_NOT_SUPPORT("crypto_algorithm_not_support", "不支持的算法"),

    /**
     * 加密错误.
     */
    ENCRYPT_ERROR("encrypt_error", "加密错误"),

    /**
     * 解密错误.
     */
    DECRYPT_ERROR("decrypt_error", "解密错误"),

    // --------------------------------------------------------------------------------------

    /**
     * excel文件格式不正确.
     */
    EXCEL_FORMAT_ERROR("excel_format_error", "excel文件格式不正确，请检查文件后缀"),

    /**
     * excel文件数据为空.
     */
    EXCEL_DATA_EMPTY("excel_data_empty", "excel文件数据为空"),

    /**
     * excel文件中有数据格式错误.
     */
    EXCEL_DATA_ERROR("excel_data_error", "excel文件中有数据错误"),

    /**
     * excel文件中数据行数不能超过.
     */
    EXCEL_ROW_TOO_BIG("excel_row_too_big", "excel文件中数据行数不能超过"),

    /**
     * excel文件导入失败，请检查文件是否错误.
     */
    EXCEL_IMPORT_ERROR("excel_import_error", "excel文件导入失败，请检查文件是否包含（'#'，'='）等开头的特殊表达式字符"),

    /**
     * excel模板错误，请选择正确的模板.
     */
    EXCEL_TEMPLATE_ERROR("excel_template_error", "excel模板错误，请选择正确的模板"),

    /**
     * excel生成错误.
     */
    EXCEL_GENERATE_ERROR("excel_generate_error", "excel生成错误"),

    // --------------------------------------------------------------------------------------

    /**
     * 最小值和最大值同时为空.
     */
    MIN_MAX_EMPTY("min_max_empty", "最小值和最大值同时为空"),

    /**
     * min和max必须同时为正数或者负数.
     */
    MIN_MAX_INVALID("min_max_invalid", "min和max必须同时为正数或者负数"),

    // --------------------------------------------------------------------------------------

    /**
     * page不合法（页面不存在或者小程序没有发布、根路径前加 /或者携带参数）.
     */
    PAGE_ILLEGAL("page_not_exist", "page不合法（页面不存在或者小程序没有发布、根路径前加 /或者携带参数）"),

    /**
     * 高风险等级用户.
     */
    HIGH_RISK_USER("high_risk_user", "高风险等级用户"),

    /**
     * 用户信息不完整.
     */
    USER_INFO_INCOMPLETE("user_info_incomplete", "用户信息不完整"),

    /**
     * 获取手机失败.
     */
    GET_PHONE_NUMBER_ERROR("get_phone_number_error", "获取手机失败"),

    /**
     * 获取用户信息失败.
     */
    GET_USER_INFO_ERROR("get_user_info_error", "获取用户信息失败"),

    /**
     * 获取二维码失败.
     */
    GET_QRCODE_ERROR("get_qrcode_error", "获取二维码失败"),

    /**
     * 微信小程序授权失败.
     */
    WECHAT_MA_GRANT_ERROR("wechat_ma_grant_error", "微信小程序授权失败"),

    /**
     * 微信公众号授权失败.
     */
    WECHAT_MP_GRANT_ERROR("wechat_mp_grant_error", "微信公众号授权授权失败"),

    /**
     * 无效的消息引擎.
     */
    INVALID_MESSAGE_ENGINE("invalid_message_engine", "无效的消息引擎"),

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
     * 通过业务编码查找枚举.
     *
     * @param bizCode 业务编码
     * @return BizCodeEnum
     */
    public static BizCodeEnum valueOfBizCode(String bizCode) {
        final Stream<BizCodeEnum> stream = Arrays.stream(BizCodeEnum.values());
        return stream.filter(m -> Objects.equals(bizCode, m.getBizCode())).findFirst().orElse(null);
    }

}
