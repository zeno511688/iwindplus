/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.ocr.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * ocr常数.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
public final class OcrConstant {
    private OcrConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 印刷文字相关常数 .
     */
    public final class PrintWordConstant {
        private PrintWordConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 印刷文字身份证图片识别接口地址.
         */
        public static final String ID_CARD_URL = "https://cardnumber.market.alicloudapi.com/rest/160601/ocr/ocr_idcard.json";

        /**
         * 印刷文字营业执照图片识别接口地址.
         */
        public static final String BUSINESS_LICENSE_URL = "https://bizlicense.market.alicloudapi.com/rest/160601/ocr/ocr_business_license.json";
    }

    /**
     * 翔云相关常数 .
     */
    public final class XiangyunConstant {
        private XiangyunConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 翔云身份证图片识别接口地址.
         */
        public static final String ID_CARD_URL = "https://netocr.com/api/recog.do";

        /**
         * 翔云营业执照图片识别接口地址.
         */
        public static final String BUSINESS_LICENSE_URL = "https://netocr.com/api/recoglen.do";

        /**
         * 翔云营业执照编码.
         */
        public static final Integer BUSINESS_LICENSE_CODE = 2008;
    }
}
