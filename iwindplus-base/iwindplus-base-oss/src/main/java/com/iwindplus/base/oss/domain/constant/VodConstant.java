/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 视频点播常数.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
public final class VodConstant {

    private VodConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 播放凭证有效时间（单位：分钟，默认：30）.
     */
    public static final int PLAY_AUTH_TIMEOUT = 30;

    /**
     * 阿里云相关常数.
     */
    public final class AliyunConstant {

        private AliyunConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 主配置编码.
         */
        public static final String MAIN_CODE = "aliyun-main";

        /**
         * 备用配置编码.
         */
        public static final String BACKUP_CODE = "aliyun-backup";
    }
}
