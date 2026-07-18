/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 告警常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class AlertConstant {

    private AlertConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 飞书相关常数.
     */
    public final class FeishuConstant {

        private FeishuConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 响应编码.
         */
        public static final String RESPONSE_CODE = "code";

        /**
         * 消息类型.
         */
        public static final String MSG_TYPE = "msg_type";

        /**
         * 内容.
         */
        public static final String CONTENT = "content";

        /**
         * 时间戳.
         */
        public static final String TIMESTAMP = "timestamp";

        /**
         * 签名.
         */
        public static final String SIGN = "sign";

    }

}
