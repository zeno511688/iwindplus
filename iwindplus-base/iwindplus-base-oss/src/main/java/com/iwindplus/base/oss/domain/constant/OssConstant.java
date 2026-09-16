/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 对象存储常数.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
public final class OssConstant {

    private OssConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * sts securityToken失效时间（单位：秒，默认：3600）.
     */
    public static final long SECURITY_TOKEN_EXPIRE_TIME = 3600L;

    /**
     * url有效时间（单位：分钟，默认：1）.
     */
    public static final int URL_TIMEOUT = 1;

    /**
     * 分片大小（单位：兆，默认：10M）.
     */
    public static final long PART_SIZE = 10L;

    /**
     * 每个分组的个数.
     */
    public static final int GROUP_SIZE = 50;

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

    /**
     * 七牛云相关常数.
     */
    public final class QiniuConstant {

        private QiniuConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 主配置编码.
         */
        public static final String MAIN_CODE = "qiniu-main";

        /**
         * 备用配置编码.
         */
        public static final String BACKUP_CODE = "qiniu-backup";
    }

    /**
     * Minio相关常数.
     */
    public final class MinioConstant {

        private MinioConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 主配置编码.
         */
        public static final String MAIN_CODE = "minio-main";

        /**
         * 备用配置编码.
         */
        public static final String BACKUP_CODE = "minio-backup";

    }
}
