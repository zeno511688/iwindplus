/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 工具常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class UtilConstant {

    private UtilConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 地址相关常数 .
     */
    public final class AddressConstant {

        private AddressConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 太平洋网络的接口.
         */
        public static final String PCONLINE_URL_STR = "http://whois.pconline.com.cn/ipJson.jsp";

        /**
         * 高德云图的接口.
         */
        public static final String GAODEYUNTU_URL_STR = "http://iploc.market.alicloudapi.com/v3/ip";

        /**
         * ip138的接口（准确）.
         */
        public static final String IP138_URL_STR = "https://api.ip138.com/ip";
    }

    /**
     * 加解密安全相关常数 .
     */
    public final class CryptoConstant {

        private CryptoConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 公钥.
         */
        public static final String PUBLIC_KEY = "publicKey";

        /**
         * 私钥.
         */
        public static final String PRIVATE_KEY = "privateKey";

        /**
         * AES模式.
         */
        public static final String AES_MODE = "GCM";

        /**
         * AES补码方式.
         */
        public static final String AES_PADDING = "NoPadding";

        /**
         * AES密钥长度.
         */
        public static final int AES_KEY_LENGTH = 32;

        /**
         * AES初始化向量长度.
         */
        public static final int AES_IV_LENGTH = 12;

        /**
         * SM4密钥长度.
         */
        public static final int SM4_KEY_LENGTH = 16;
    }
}
