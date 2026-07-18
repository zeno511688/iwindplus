/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 短信常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class SmsConstant {

    private SmsConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 阿里云相关常数.
     */
    public final class AliyunConstant {

        private AliyunConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * sts securityToken失效时间（单位：秒，默认：3600）.
         */
        public static final long SECURITY_TOKEN_EXPIRE_TIME = 3600L;

        /**
         * 响应编码.
         */
        public static final String RESPONSE_CODE = "Code";

        /**
         * 业务流水号.
         */
        public static final String BIZ_NUMBER = "BizId";

        /**
         * MOBILE_NUMBER_ILLEGAL.
         */
        public static final String MOBILE_NUMBER_ILLEGAL = "isv.MOBILE_NUMBER_ILLEGAL";

        /**
         * BUSINESS_LIMIT_CONTROL.
         */
        public static final String BUSINESS_LIMIT_CONTROL = "isv.BUSINESS_LIMIT_CONTROL";

        /**
         * PARAM_NOT_SUPPORT_URL.
         */
        public static final String PARAM_NOT_SUPPORT_URL = "isv.PARAM_NOT_SUPPORT_URL";

        /**
         * AMOUNT_NOT_ENOUGH.
         */
        public static final String AMOUNT_NOT_ENOUGH = "isv.AMOUNT_NOT_ENOUGH";
    }

    /**
     * 七牛云相关常数.
     */
    public final class QiniuConstant {

        private QiniuConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 业务流水号.
         */
        public static final String BIZ_NUMBER = "job_id";
    }

    /**
     * 凌凯相关常数.
     */
    public final class LingKaiConstant {

        private LingKaiConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 凌凯短信接口地址.
         */
        public static final String LING_KAI_SMS_URL = "https://mb345.com/ws/BatchSend2.aspx";

        /**
         * 访问key.
         */
        public static final String ACCESS_KEY = "CorpID";

        /**
         * 密钥.
         */
        public static final String SECRET_KEY = "Pwd";

        /**
         * 手机.
         */
        public static final String MOBILE = "Mobile";

        /**
         * 内容.
         */
        public static final String CONTENT = "Content";
    }

    /**
     * 麦讯通相关常数.
     */
    public final class MxtongConstant {

        private MxtongConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 麦讯通短信接口地址.
         */
        public static final String MX_TONG_SMS_URL = "http://www.weiwebs.cn/msg/HttpBatchSendSM";

        /**
         * 访问key.
         */
        public static final String ACCESS_KEY = "account";

        /**
         * 密钥.
         */
        public static final String SECRET_KEY = "pswd";

        /**
         * 手机.
         */
        public static final String MOBILE = "mobile";

        /**
         * 内容.
         */
        public static final String CONTENT = "msg";

        /**
         * 是否返回状态.
         */
        public static final String NEED_STATUS = "needstatus";

        /**
         * 响应类型.
         */
        public static final String RESPONSE_TYPE = "json";

        /**
         * 响应编码.
         */
        public static final String RESPONSE_CODE = "result";

        /**
         * 业务流水号.
         */
        public static final String BIZ_NUMBER = "msgid";
    }

    /**
     * 每个分组的手机个数.
     */
    public static final int PHONE_NUMBER_GROUP_SIZE = 100;

    /**
     * 短信验证码长度.
     */
    public static final int CAPTCHA_LENGTH = 6;

    /**
     * 短信验证码有效时间（单位：分钟，默认：10）.
     */
    public static final int CAPTCHA_TIMEOUT = 10;
}
