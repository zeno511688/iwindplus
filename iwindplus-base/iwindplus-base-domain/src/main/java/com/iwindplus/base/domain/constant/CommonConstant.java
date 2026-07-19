/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.constant;

import java.util.Set;

/**
 * 公共常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class CommonConstant {

    private CommonConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * Utility class.
     */
    public static final String UTILITY_CLASS = "Utility class";

    /**
     * bean相关常数 .
     */
    public final class BeanConstant {

        private BeanConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * keyGenerator.
         */
        public static final String BEAN_KEY_GENERATOR = "keyGenerator";
    }

    /**
     * JWT相关常数 .
     */
    public final class JwtConstant {

        private JwtConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 过期时间.
         */
        public static final String EXP = "exp";
    }

    /**
     * 系统相关常数 .
     */
    public final class SystemConstant {

        private SystemConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 系统.
         */
        public static final String SYSTEM = "system";

        /**
         * unknown.
         */
        public static final String UNKNOWN = "unknown";

        /**
         * 无国际化信息.
         */
        public static final String NO_I18N_MESSAGE = "No i18n message";

        /**
         * 默认.
         */
        public static final String DEFAULT = "default";

        /**
         * 类.
         */
        public static final String CLASS = "class";

        /**
         * 空.
         */
        public static final String NULL = "null";

        /**
         * 请求ID.
         */
        public static final String REQUEST_ID = "requestId";

        /**
         * OpenTelemetry baggage.
         */
        public static final String OT_BAGGAGE = "ot-baggage-";

        /**
         * trace相关常数.
         */
        public static final Set<String> TRACE_HEADERS =
            Set.of(
                // W3C OpenTelemetry
                "traceparent",
                "tracestate",
                "baggage",
                // B3
                "b3",
                "x-b3-traceid",
                "x-b3-spanid",
                "x-b3-parentspanid",
                "x-b3-sampled",
                "x-b3-flags",
                // Jaeger
                "uber-trace-id",
                // AWS X-Ray
                "x-amzn-trace-id",
                // Datadog
                "x-datadog-trace-id",
                "x-datadog-parent-id",
                "x-datadog-sampling-priority",
                "x-datadog-origin",
                "x-datadog-tags",
                // SkyWalking
                "sw8",
                "sw8-x",
                // Elastic APM
                "elastic-apm-traceparent",
                // 历史自定义
                "trace-id",
                "traceid",
                "trace_id",
                "span-id",
                "spanid",
                "span_id",
                "parent-span-id",
                "parentspanid",
                "parent_span_id"
            );
    }

    /**
     * 观察相关常数（Observation（单次请求）） .
     */
    public final class ObservationConstant {

        private ObservationConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 名称.
         * <p>
         * 对齐 Spring Boot 3 Observation http client 标准名称
         */
        public static final String HTTP_OBSERVATION_NAME = "http.client.requests";

        /**
         * 应用.
         * <p>
         * 对应 Micrometer application tag
         */
        public static final String APPLICATION = "application";

        /**
         * 客户端名称.
         * <p>
         * 对齐 Micrometer client.name
         */
        public static final String CLIENT_NAME = "client.name";

        /**
         * 请求方式.
         */
        public static final String HTTP_METHOD = "http.method";

        /**
         * 路径.
         * <p>
         * 必须为 path template（如 /orders/{id}）
         */
        public static final String HTTP_URL = "http.url";

        /**
         * 状态.
         * <p>
         * HTTP status 或 IO_ERROR
         */
        public static final String HTTP_STATUS = "http.status";

        /**
         * 请求结果.
         * <p>
         * SUCCESS / ERROR 与 Micrometer outcome 语义完全一致
         */
        public static final String OUTCOME = "outcome";

        /**
         * 错误.
         * <p>
         * NONE / ExceptionSimpleName
         */
        public static final String EXCEPTION = "exception";

        /**
         * 成功.
         */
        public static final String OUTCOME_SUCCESS = "SUCCESS";

        /**
         * 错误.
         */
        public static final String OUTCOME_ERROR = "ERROR";

        /**
         * IO_ERROR.
         */
        public static final String IO_ERROR = "IO_ERROR";

        /**
         * NONE.
         */
        public static final String NONE = "NONE";
    }

    /**
     * 限流熔断相关常数（趋势） .
     */
    public final class CircuitConstant {

        private CircuitConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 名称.
         */
        public static final String CIRCUIT_BREAKER_NAME = "circuit-breaker";
    }

    /**
     * Header请求相关常数.
     */
    public final class HeaderConstant {

        private HeaderConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * BEARER_TYPE.
         */
        public static final String BEARER_TYPE = "Bearer";

        /**
         * 转发ip列表.
         */
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";

        /**
         * 转发前缀.
         */
        public static final String X_FORWARDED_PREFIX = "X-Forwarded-Prefix";

        /**
         * 请求唯一标识.
         */
        public static final String X_REQUESTED_ID = "X-Requested-Id";

        /**
         * X-Requested-With.
         */
        public static final String X_REQUESTED_WITH = "X-Requested-With";

        /**
         * XMLHttpRequest.
         */
        public static final String XML_HTTP_REQUEST = "XMLHttpRequest";

        /**
         * Sec-WebSocket-Protocol.
         */
        public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

        /**
         * 追踪父级标识.
         */
        public static final String TRACE_PARENT = "traceparent";

        /**
         * 跟踪唯一标识.
         */
        public static final String TRACE_ID = "traceId";

        /**
         * 真实ip.
         */
        public static final String REAL_IP = "realIp";

        /**
         * 用户信息.
         */
        public static final String X_USER_INFO = "X-User-Info";

        /**
         * 灰度发布版本号.
         */
        public static final String X_VERSION = "X-Version";

        /**
         * ga验证码.
         */
        public static final String X_GA_CAPTCHA = "X-Ga-Captcha";

        /**
         * 邮箱验证码.
         */
        public static final String X_MAIL_CAPTCHA = "X-Mail-Captcha";

        /**
         * 短信验证码.
         */
        public static final String X_SMS_CAPTCHA = "X-Sms-Captcha";

        /**
         * tcc全局事务ID.
         */
        public static final String X_TCC_XID = "X-Tcc-Xid";

        /**
         * yubikey原始数据.
         */
        public static final String X_YUBIKEY_SOURCE = "X-Yubikey-Source";

        /**
         * yubikey签名数据.
         */
        public static final String X_YUBIKEY_SIGN = "X-Yubikey-Sign";
    }

    /**
     * DB签名相关常数.
     */
    public final class DbSignConstant {

        private DbSignConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 数据库名称.
         */
        public static final String DB_NAME = "dbName";

        /**
         * 表名.
         */
        public static final String TABLE_NAME = "tableName";

        /**
         * 操作（增删改查）.
         */
        public static final String ACTION = "action";

        /**
         * 加密盐.
         */
        public static final String SALT = "salt";

        /**
         * 签名.
         */
        public static final String SIGN = "sign";
    }

    /**
     * API签名相关常数.
     */
    public final class ApiSignConstant {

        private ApiSignConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 访问key.
         */
        public static final String X_ACCESS_KEY = "X-Access-Key";

        /**
         * 时间戳.
         */
        public static final String X_TIMESTAMP = "X-Timestamp";

        /**
         * nonce.
         */
        public static final String X_NONCE = "X-Nonce";

        /**
         * 请求路径.
         */
        public static final String X_PATH = "X-Path";

        /**
         * 请求方式.
         */
        public static final String X_METHOD = "X-Method";

        /**
         * 签名.
         */
        public static final String X_SIGN = "X-Sign";

        /**
         * 应用（可选）.
         */
        public static final String APPLICATION = "application";
    }

    /**
     * Response相关常数.
     */
    public final class ResponseConstant {

        private ResponseConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 业务编码.
         */
        public static final String BIZ_CODE = "bizCode";

        /**
         * 业务信息.
         */
        public static final String BIZ_MESSAGE = "bizMessage";

        /**
         * 业务信息参数.
         */
        public static final String BIZ_MESSAGE_PARAMS = "bizMessageParams";

        /**
         * 业务数据.
         */
        public static final String BIZ_DATA = "bizData";

        /**
         * 业务时间.
         */
        public static final String BIZ_TIME = "bizTime";

        /**
         * 业务时间戳.
         */
        public static final String BIZ_TIMESTAMP = "bizTimestamp";

        /**
         * 业务跟踪唯一标识.
         */
        public static final String BIZ_TRACE_ID = "bizTraceId";
    }

    /**
     * 用户相关常数.
     */
    public final class UserConstant {

        private UserConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 用户主键.
         */
        public static final String USER_ID = "userId";

        /**
         * 组织主键.
         */
        public static final String ORG_ID = "orgId";

        /**
         * 工号.
         */
        public static final String JOB_NUMBER = "jobNumber";

        /**
         * 用户名.
         */
        public static final String USERNAME = "username";

        /**
         * 密码.
         */
        public static final String PASSWORD = "password";

        /**
         * 手机.
         */
        public static final String MOBILE = "mobile";

        /**
         * 姓名.
         */
        public static final String REAL_NAME = "realName";

        /**
         * 性别.
         */
        public static final String SEX = "sex";

        /**
         * 邮箱.
         */
        public static final String MAIL = "mail";

        /**
         * 昵称.
         */
        public static final String NICK_NAME = "nickName";

        /**
         * 头像.
         */
        public static final String AVATAR = "avatar";

        /**
         * 身份证.
         */
        public static final String ID_CARD = "idCard";

        /**
         * 是否管理员.
         */
        public static final String ADMIN_FLAG = "adminFlag";

        /**
         * GA密钥.
         */
        public static final String GA_SECRET = "gaSecret";

        /**
         * 权限集合
         */
        public static final String PERMISSIONS = "permissions";
    }

    /**
     * Db相关常数.
     */
    public final class DbConstant {

        private DbConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 主键.
         */
        public static final String ID = "id";

        /**
         * 创建时间.
         */
        public static final String CREATED_TIME = "createdTime";

        /**
         * 创建时间戳.
         */
        public static final String CREATED_TIMESTAMP = "createdTimestamp";

        /**
         * 创建人.
         */
        public static final String CREATED_BY = "createdBy";

        /**
         * 创建人主键.
         */
        public static final String CREATED_ID = "createdId";

        /**
         * 更新时间.
         */
        public static final String MODIFIED_TIME = "modifiedTime";

        /**
         * 更新时间戳.
         */
        public static final String MODIFIED_TIMESTAMP = "modifiedTimestamp";

        /**
         * 更新人.
         */
        public static final String MODIFIED_BY = "modifiedBy";

        /**
         * 更新人主键.
         */
        public static final String MODIFIED_ID = "modifiedId";

        /**
         * 是否删除.
         */
        public static final String DELETED = "deleted";

        /**
         * 乐观锁.
         */
        public static final String VERSION = "version";

        /**
         * 备注.
         */
        public static final String REMARK = "remark";

        /**
         * 加密盐.
         */
        public static final String SALT = "salt";

        /**
         * 签名.
         */
        public static final String SIGN = "sign";
    }

    /**
     * 网关路由相关常数.
     */
    public static class GatewayRouteConstant {

        private GatewayRouteConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 网关服务路由文件名.
         */
        public static final String GATEWAY_ROUTE_FILE_NAME = "gateway-route.json";

        /**
         * 网关服务路由分组.
         */
        public static final String GATEWAY_GROUP = "GATEWAY_GROUP";

        /**
         * 路径.
         */
        public static final String PATH = "Path";

        /**
         * 动态路由key.
         */
        public static final String GENKEY_0 = "_genkey_0";
    }

    /**
     * 元数据相关常数.
     */
    public final class MetadataConstant {

        private MetadataConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 名称.
         */
        public static final String NAME = "name";

        /**
         * 版本.
         */
        public static final String VERSION = "version";

        /**
         * 权重.
         */
        public static final String WEIGHT = "weight";
    }

    /**
     * 网络相关常数.
     */
    public final class NetWorkConstant {

        private NetWorkConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * http.
         */
        public static final String HTTP = "http";

        /**
         * https.
         */
        public static final String HTTPS = "https";

        /**
         * ws.
         */
        public static final String WS = "ws";

        /**
         * wss.
         */
        public static final String WSS = "wss";

        /**
         * lb.
         */
        public static final String LB = "lb";

        /**
         * http://.
         */
        public static final String HTTP_PREFIX = "http://";

        /**
         * https://.
         */
        public static final String HTTPS_PREFIX = "https://";

        /**
         * ws://.
         */
        public static final String WS_PREFIX = "ws://";

        /**
         * wss://.
         */
        public static final String WSS_PREFIX = "wss://";

        /**
         * lb://.
         */
        public static final String LB_PREFIX = "lb://";

        /**
         * localhost.
         */
        public static final String LOCALHOST = "localhost";
    }

    /**
     * 符号相关常数.
     */
    public final class SymbolConstant {

        private SymbolConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 空串.
         */
        public static final String EMPTY_STR = "";

        /**
         * 问号.
         */
        public static final String QUESTION_MARK = "?";

        /**
         * 点.
         */
        public static final String POINT = ".";

        /**
         * 斜杠.
         */
        public static final String SLASH = "/";

        /**
         * 美元符号.
         */
        public static final String DOLLAR = "$";

        /**
         * 人民币符号.
         */
        public static final String RMB = "¥";

        /**
         * 冒号.
         */
        public static final String COLON = ":";

        /**
         * 双冒号.
         */
        public static final String DOUBLE_COLON = "::";

        /**
         * 分号.
         */
        public static final String SEMICOLON = ";";

        /**
         * 下划线.
         */
        public static final String UNDERLINE = "_";

        /**
         * 横线.
         */
        public static final String HORIZONTAL_LINE = "-";

        /**
         * 星号.
         */
        public static final String ASTERISK = "*";

        /**
         * 逗号.
         */
        public static final String COMMA = ",";

        /**
         * 逻辑与.
         */
        public static final String LOGICAL_AND = "&";

        /**
         * 等于.
         */
        public static final String EQUAL = "=";

        /**
         * 艾特符号.
         */
        public static final String AT = "@";

        /**
         * 竖杠.
         */
        public static final String PIPE = "|";

        /**
         * 双竖杠.
         */
        public static final String DOUBLE_PIPE = "||";

        /**
         * 井号.
         */
        public static final String WELL_NO = "#";

        /**
         * 花括号.
         */
        public static final String CURLY_BRACKET = "{}";

        /**
         * 左花括号.
         */
        public static final String LEFT_CURLY_BRACKET = "{";

        /**
         * 井号 + 左花括号.
         */
        public static final String WELL_NO_AND_LEFT_CURLY_BRACKET = "#{";

        /**
         * 美元符号 + 左花括号.
         */
        public static final String DOLLAR_AND_LEFT_CURLY_BRACKET = "${";

        /**
         * 双美元符号.
         */
        public static final String DOUBLE_DOLLAR = "$$";

        /**
         * 右花括号.
         */
        public static final String RIGHT_CURLY_BRACKET = "}";

        /**
         * 左中括号.
         */
        public static final String LEFT_SQUARE_BRACKET = "[";

        /**
         * 右中括号.
         */
        public static final String RIGHT_SQUARE_BRACKET = "]";

        /**
         * 感叹号.
         */
        public static final String EXCLAMATION_MARK = "!";

        /**
         * 斜杠星号.
         */
        public static final String SLASH_ASTERISK = "/*";

        /**
         * 换行符.
         */
        public static final String NEWLINE = "\n";

        /**
         * url规则.
         */
        public static final String BASE_PATH = "/**";

        /**
         * 反斜杠.
         */
        public static final String BACK_SLASH = "\\";

    }

    /**
     * 文件相关常数.
     */
    public final class FileConstant {

        private FileConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 用户目录.
         */
        public static final String USER_DIR = "user.dir";

        /**
         * 缓存目录.
         */
        public static final String TMP_DIR = "java.io.tmpdir";

        /**
         * 文件.
         */
        public static final String FILE = "file";

        /**
         * 文件限制4M.
         */
        public static final long FILE_SIZE = 4194304;

        /**
         * 分片限制总数.
         */
        public static final long PART_COUNT = 10000;

        /**
         * byte限制大小.
         */
        public static final int BYTE_SIZE = 8192;

        /**
         * classpath.
         */
        public static final String CLASSPATH = "classpath";

        /**
         * class.
         */
        public static final String MORE_CLASS = "/**/*.class";
    }

    /**
     * Oauth相关常数.
     */
    public final class OauthConstant {

        private OauthConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 授权类型.
         */
        public static final String GRANT_TYPE = "grant_type";

        /**
         * token类型.
         */
        public static final String TOKEN_TYPE = "token_type";

        /**
         * 访问token过期时间.
         */
        public static final String EXPIRES_IN = "expires_in";

        /**
         * 客户端ID.
         */
        public static final String CLIENT_ID = "client_id";

        /**
         * 客户端密匙.
         */
        public static final String CLIENT_SECRET = "client_secret";

        /**
         * 访问token.
         */
        public static final String ACCESS_TOKEN = "access_token";

        /**
         * 刷新token.
         */
        public static final String REFRESH_TOKEN = "refresh_token";

        /**
         * 认证类型
         */
        public static final String AUTHORIZATION_TYPE = "authorization_type";
    }

    /**
     * 数字相关常数 .
     */
    public final class NumberConstant {

        private NumberConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * -1.
         */
        public static final int NUMBER_NEGATIVE_ONE = -1;

        /**
         * 0.
         */
        public static final int NUMBER_ZERO = 0;

        /**
         * 1.
         */
        public static final int NUMBER_ONE = 1;

        /**
         * 2.
         */
        public static final int NUMBER_TWO = 2;

        /**
         * 3.
         */
        public static final int NUMBER_THREE = 3;

        /**
         * 4.
         */
        public static final int NUMBER_FOUR = 4;

        /**
         * 5.
         */
        public static final int NUMBER_FIVE = 5;

        /**
         * 6.
         */
        public static final int NUMBER_SIX = 6;

        /**
         * 7.
         */
        public static final int NUMBER_SEVEN = 7;

        /**
         * 8.
         */
        public static final int NUMBER_EIGHT = 8;

        /**
         * 9.
         */
        public static final int NUMBER_NINE = 9;

        /**
         * 10.
         */
        public static final int NUMBER_TEN = 10;

        /**
         * 15.
         */
        public static final int NUMBER_FIFTEEN = 15;

        /**
         * 16.
         */
        public static final int NUMBER_SIXTEEN = 16;

        /**
         * 20.
         */
        public static final int NUMBER_TWENTY = 20;

        /**
         * 25.
         */
        public static final int NUMBER_TWENTY_FIVE = 25;

        /**
         * 26.
         */
        public static final int NUMBER_TWENTY_SIX = 26;

        /**
         * 30.
         */
        public static final int NUMBER_THIRTY = 30;

        /**
         * 50.
         */
        public static final int NUMBER_FIFTY = 50;

        /**
         * 51.
         */
        public static final int NUMBER_FIFTY_ONE = 51;

        /**
         * 60.
         */
        public static final int NUMBER_SIXTY = 60;

        /**
         * 64.
         */
        public static final int NUMBER_SIXTY_FOUR = 64;

        /**
         * 100.
         */
        public static final int NUMBER_ONE_HUNDRED = 100;

        /**
         * 200.
         */
        public static final int NUMBER_TWO_HUNDRED = 200;

        /**
         * 300.
         */
        public static final int NUMBER_THREE_HUNDRED = 300;

        /**
         * 500.
         */
        public static final int NUMBER_FIVE_HUNDRED = 500;

        /**
         * 512.
         */
        public static final int NUMBER_FIVE_HUNDRED_TWELVE = 512;

        /**
         * 599.
         */
        public static final int NUMBER_FIVE_HUNDRED_AND_NINETY_NINE = 599;

        /**
         * 999.
         */
        public static final int NUMBER_NINE_HUNDRED_AND_NINETY_NINE = 999;

        /**
         * 1000.
         */
        public static final int NUMBER_ONE_THOUSAND = 1000;

        /**
         * 1024.
         */
        public static final int NUMBER_ONE_THOUSAND_TWENTY_FOUR = 1024;

        /**
         * 1800.
         */
        public static final int NUMBER_ONE_THOUSAND_AND_EIGHT_HUNDRED = 1800;

        /**
         * 2000.
         */
        public static final int NUMBER_TWO_THOUSAND = 2000;

        /**
         * 2048.
         */
        public static final int NUMBER_TWO_THOUSAND_FORTY_EIGHT = 2048;

        /**
         * 3000.
         */
        public static final int NUMBER_THREE_THOUSAND = 3000;

        /**
         * 4000.
         */
        public static final int NUMBER_FOUR_THOUSAND = 4000;

        /**
         * 4096.
         */
        public static final int NUMBER_FOUR_THOUSAND_NINETY_SIX = 4096;

        /**
         * 5000.
         */
        public static final int NUMBER_FIVE_THOUSAND = 5000;

        /**
         * 8192.
         */
        public static final int NUMBER_EIGHT_THOUSAND_ONE_THOUSAND_NINETY_TWO = 8192;

        /**
         * 10000.
         */
        public static final int NUMBER_TEN_THOUSAND = 10000;

        /**
         * 50000.
         */
        public static final int NUMBER_FIFTY_THOUSAND = 50000;

        /**
         * 60000.
         */
        public static final int NUMBER_SIXTY_THOUSAND = 60000;
    }

    /**
     * excel相关常数 .
     */
    public final class ExcelConstant {

        private ExcelConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * xls.
         */
        public static final String XLS = "xls";

        /**
         * xlsx.
         */
        public static final String XLSX = "xlsx";

        /**
         * excel 2003最大行数.
         */
        public static final int EXCEL_2003_MAX_ROW = 65536;

        /**
         * excel 2007最大行数.
         */
        public static final int EXCEL_2007_MAX_ROW = 1048576;

        /**
         * excel 导入支持的最大行数.
         */
        public static final int EXCEL_MAX_ROW = 1000;

        /**
         * 隐藏的工作表名称.
         */
        public static final String HIDDEN_SHEET_NAME = "hiddenSheet";
    }

    /**
     * 日期相关常数 .
     */
    public final class DateConstant {

        private DateConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * yyMMdd.
         */
        public static final String YYMMDD = "yyMMdd";
    }

    /**
     * 正则相关常数 .
     */
    public final class RegexConstant {

        private RegexConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * ipv4 校验表达式.
         */
        public static final String IPV4_REGEX = "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$";

        /**
         * 英文 校验表达式.
         */
        public static final String ENGLISH_REGEX = "^[A-Za-z]+$";

        /**
         * 匹配任意空白字符（包括空格、制表符、换行符等）.
         */
        public static final String WHITE_SPACE_REGEX = "[\\s]+";
    }

    /**
     * 分页常数.
     *
     * @author zengdegui
     * @since 2023/07/30 21:50
     */
    public final class PageConstant {

        private PageConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 记录.
         */
        public static final String RECORDS = "records";

        /**
         * 总数.
         */
        public static final String TOTAL = "total";

        /**
         * 总页数.
         */
        public static final String PAGES = "pages";

        /**
         * 当前页.
         */
        public static final String CURRENT = "current";

        /**
         * 每页显示条数.
         */
        public static final String SIZE = "size";
    }

    /**
     * 异常常数.
     *
     * @author zengdegui
     * @since 2023/07/30 21:50
     */
    public final class ExceptionConstant {

        private ExceptionConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * UnknownHostException.
         */
        public static final String UNKNOWN_HOST_EXCEPTION = "UnknownHostException";

        /**
         * TimeoutException.
         */
        public static final String TIMEOUT_EXCEPTION = "TimeoutException";

        /**
         * SocketException.
         */
        public static final String SOCKET_EXCEPTION = "SocketException";

        /**
         * ServiceUnavailableException.
         */
        public static final String SERVICE_UNAVAILABLE_EXCEPTION = "ServiceUnavailableException";

        /**
         * UnauthorizedException.
         */
        public static final String UNAUTHORIZED_EXCEPTION = "UnauthorizedException";

        /**
         * NoResourceFoundException.
         */
        public static final String NO_RESOURCE_FOUND_EXCEPTION = "NoResourceFoundException";

        /**
         * NoHandlerFoundException.
         */
        public static final String NO_HANDLER_FOUND_EXCEPTION = "NoHandlerFoundException";

        /**
         * HttpRequestMethodNotSupportedException.
         */
        public static final String HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION = "HttpRequestMethodNotSupportedException";

        /**
         * HttpMediaTypeNotSupportedException.
         */
        public static final String HTTP_MEDIA_TYPE_NOT_SUPPORTED_EXCEPTION = "HttpMediaTypeNotSupportedException";

        /**
         * HttpMediaTypeNotAcceptableException.
         */
        public static final String HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_EXCEPTION = "HttpMediaTypeNotAcceptableException";

        /**
         * HttpMessageNotReadableException.
         */
        public static final String HTTP_MESSAGE_NOT_READABLE_EXCEPTION = "HttpMessageNotReadableException";

        /**
         * HttpMessageNotWritableException.
         */
        public static final String HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION = "HttpMessageNotWritableException";

        /**
         * NullPointerException.
         */
        public static final String NULL_POINTER_EXCEPTION = "NullPointerException";

        /**
         * ConversionNotSupportedException.
         */
        public static final String CONVERSION_NOT_SUPPORTED_EXCEPTION = "ConversionNotSupportedException";

        /**
         * IllegalArgumentException.
         */
        public static final String ILLEGAL_ARGUMENT_EXCEPTION = "IllegalArgumentException";

        /**
         * NotFoundException.
         */
        public static final String NOT_FOUND_EXCEPTION = "NotFoundException";

        /**
         * FileNotFoundException.
         */
        public static final String FILE_NOT_FOUND_EXCEPTION = "FileNotFoundException";

        /**
         * DecodingException.
         */
        public static final String DECODING_EXCEPTION = "DecodingException";

        /**
         * ClassCastException.
         */
        public static final String CLASS_CAST_EXCEPTION = "ClassCastException";

        /**
         * NumberFormatException.
         */
        public static final String NUMBER_FORMAT_EXCEPTION = "NumberFormatException";

        /**
         * SecurityException.
         */
        public static final String SECURITY_EXCEPTION = "SecurityException";

        /**
         * BadSqlGrammarException.
         */
        public static final String BAD_SQL_GRAMMAR_EXCEPTION = "BadSqlGrammarException";

        /**
         * SQLException.
         */
        public static final String SQL_EXCEPTION = "SQLException";

        /**
         * DataIntegrityViolationException.
         */
        public static final String DATA_INTEGRITY_VIOLATION_EXCEPTION = "DataIntegrityViolationException";

        /**
         * TypeNotPresentException.
         */
        public static final String TYPE_NOT_PRESENT_EXCEPTION = "TypeNotPresentException";

        /**
         * IOException.
         */
        public static final String IO_EXCEPTION = "IOException";

        /**
         * NoSuchMethodException.
         */
        public static final String NO_SUCH_METHOD_EXCEPTION = "NoSuchMethodException";

        /**
         * IndexOutOfBoundsException.
         */
        public static final String INDEX_OUT_OF_BOUNDS_EXCEPTION = "IndexOutOfBoundsException";

        /**
         * NoSuchBeanDefinitionException.
         */
        public static final String NO_SUCH_BEAN_DEFINITION_EXCEPTION = "NoSuchBeanDefinitionException";

        /**
         * TypeMismatchException.
         */
        public static final String TYPE_MISMATCH_EXCEPTION = "TypeMismatchException";

        /**
         * MissingServletRequestPartException.
         */
        public static final String MISSING_SERVLET_REQUEST_PART_EXCEPTION = "MissingServletRequestPartException";

        /**
         * MaxUploadSizeExceededException.
         */
        public static final String MAX_UPLOAD_SIZE_EXCEEDED_EXCEPTION = "MaxUploadSizeExceededException";

        /**
         * MultipartException.
         */
        public static final String MULTIPART_EXCEPTION = "MultipartException";

        /**
         * StackOverflowError.
         */
        public static final String STACK_OVERFLOW_ERROR = "StackOverflowError";

        /**
         * ArithmeticException.
         */
        public static final String ARITHMETIC_EXCEPTION = "ArithmeticException";

        /**
         * MissingServletRequestParameterException.
         */
        public static final String MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION = "MissingServletRequestParameterException";

        /**
         * MethodArgumentTypeMismatchException.
         */
        public static final String METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION = "MethodArgumentTypeMismatchException";

        /**
         * ServerWebInputException.
         */
        public static final String SERVER_WEB_INPUT_EXCEPTION = "ServerWebInputException";

        /**
         * BindException.
         */
        public static final String BIND_EXCEPTION = "BindException";

        /**
         * ConstraintViolationException.
         */
        public static final String CONSTRAINT_VIOLATION_EXCEPTION = "ConstraintViolationException";

        /**
         * MethodArgumentNotValidException.
         */
        public static final String METHOD_ARGUMENT_NOT_VALID_EXCEPTION = "MethodArgumentNotValidException";

        /**
         * MailSendException.
         */
        public static final String MAIL_SEND_EXCEPTION = "MailSendException";

        /**
         * UnsupportedOperationException.
         */
        public static final String UNSUPPORTED_OPERATION_EXCEPTION = "UnsupportedOperationException";

        /**
         * ParseException.
         */
        public static final String PARSE_EXCEPTION = "ParseException";

        /**
         * SerializationException.
         */
        public static final String SERIALIZATION_EXCEPTION = "SerializationException";

        /**
         * JsonProcessingException.
         */
        public static final String JSON_PROCESSING_EXCEPTION = "JsonProcessingException";

        /**
         * JsonMappingException.
         */
        public static final String JSON_MAPPING_EXCEPTION = "JsonMappingException";

        /**
         * MyBatisSystemException.
         */
        public static final String MYBATIS_SYSTEM_EXCEPTION = "MyBatisSystemException";

        /**
         * URISyntaxException.
         */
        public static final String URI_SYNTAX_EXCEPTION = "URISyntaxException";

        /**
         * Exception.
         */
        public static final String EXCEPTION = "Exception";

        /**
         * IllegalStateException.
         */
        public static final String ILLEGAL_STATE_EXCEPTION = "IllegalStateException";

        /**
         * UnsupportedEncodingException.
         */
        public static final String UNSUPPORTED_ENCODING_EXCEPTION = "UnsupportedEncodingException";

        /**
         * MessagingException.
         */
        public static final String MESSAGING_EXCEPTION = "MessagingException";

        /**
         * IllegalAccessException.
         */
        public static final String ILLEGAL_ACCESS_EXCEPTION = "IllegalAccessException";

        /**
         * NoSuchFieldException.
         */
        public static final String NO_SUCH_FIELD_EXCEPTION = "NoSuchFieldException";

        /**
         * InterruptedException.
         */
        public static final String INTERRUPTED_EXCEPTION = "InterruptedException";

        /**
         * ExecutionException.
         */
        public static final String EXECUTION_EXCEPTION = "ExecutionException";
    }
}
