/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss.domain.property;

import com.iwindplus.base.domain.dto.AkSkDTO;
import com.iwindplus.base.domain.dto.StsTokenDTO;
import java.util.ArrayList;
import java.util.List;
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
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 默认配置编码（可选）.
     */
    private String defaultCode;

    /**
     * 阿里云对象存储配置列表.
     */
    @Builder.Default
    private List<AliyunConfig> aliyun = new ArrayList<>(10);

    /**
     * 七牛云对象存储配置列表.
     */
    @Builder.Default
    private List<QiniuConfig> qiniu = new ArrayList<>(10);

    /**
     * Minio对象存储配置列表.
     */
    @Builder.Default
    private List<MinioConfig> minio = new ArrayList<>(10);

    /**
     * OSS基础配置.
     *
     * @author zengdegui
     * @since 2026/9/4
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public abstract static class BaseConfig extends AkSkDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 配置编码（唯一标识）.
         */
        private String code;

        /**
         * 配置名称.
         */
        private String name;

        /**
         * 优先级（数字越小优先级越高，用于自动故障转移）.
         */
        private Integer priority;
    }

    /**
     * 阿里云对象存储相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AliyunConfig extends BaseConfig {

        /**
         * oss地域节点（必填，如：oss-cn-shenzhen.aliyuncs.com）.
         */
        private String endpoint;

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
        @Builder.Default
        @NestedConfigurationProperty
        private StsTokenDTO sts = new StsTokenDTO();
    }

    /**
     * 七牛云对象存储相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QiniuConfig extends BaseConfig {

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
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MinioConfig extends BaseConfig {

        /**
         * oss地域节点（必填）.
         */
        private String endpoint;

        /**
         * 区域（可选）.
         */
        private String region;

        /**
         * 分片大小（可选，单位：兆，默认：10M）.
         */
        private Long partSize;
    }
}

