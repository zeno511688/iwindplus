/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.property;

import com.iwindplus.base.domain.dto.AkSkDTO;
import com.iwindplus.base.oss.domain.dto.StsTokenDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 对象存储相关属性.
 *
 * @author zengdegui
 * @since 2023/6/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "oss")
public class OssProperty {

    /**
     * 阿里云对象存储配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private AliyunConfig aliyun = new AliyunConfig();

    /**
     * 七牛云对象存储配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private QiniuConfig qiniu = new QiniuConfig();

    /**
     * Minio对象存储配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private MinioConfig minio = new MinioConfig();

    /**
     * 阿里云对象存储相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper=false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AliyunConfig extends AkSkDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * oss地域节点（必填，如：oss-cn-shenzhen.aliyuncs.com）.
         */
        private String endpoint;

        /**
         * 空间名（必填）.
         */
        private String bucketName;

        /**
         * 访问域名（可选，自定义域名）.
         */
        private String accessDomain;

        /**
         * 分片大小（可选，单位：兆，默认：10M）.
         */
        private Long partSize;

        /**
         * 是否开启断点上传（可选，默认：false）.
         */
        private Boolean broke;

        /**
         * sts配置（可选）.
         */
        @NestedConfigurationProperty
        private StsTokenDTO sts;
    }

    /**
     * 七牛云对象存储相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper=false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QiniuConfig extends AkSkDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * 空间名（必填）.
         */
        private String bucketName;

        /**
         * 访问域名（必填，自定义域名）.
         */
        private String accessDomain;

        /**
         * 分片大小（可选，单位：兆，默认：10M）.
         */
        private Long partSize;

        /**
         * 是否开启断点上传（可选，默认：false）.
         */
        private Boolean broke;
    }

    /**
     * Minio对象存储相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper=false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MinioConfig extends AkSkDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

        /**
         * oss地域节点（必填）.
         */
        private String endpoint;

        /**
         * 区域（可选）.
         */
        private String region;

        /**
         * 空间名（必填）.
         */
        private String bucketName;

        /**
         * 访问域名（可选，自定义域名）.
         */
        private String accessDomain;

        /**
         * 分片大小（可选，单位：兆，默认：10M）.
         */
        private Long partSize;
    }
}

