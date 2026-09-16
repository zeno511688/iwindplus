/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.domain.property;

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
 * 短信相关属性.
 *
 * @author zengdegui
 * @since 2023/6/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "sms")
public class SmsProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 是否启用自动故障转移（默认：true）.
     */
    @Builder.Default
    private Boolean enabledFailover = Boolean.TRUE;

    /**
     * 阿里云短信配置列表.
     */
    @Builder.Default
    private List<AliyunConfig> aliyun = new ArrayList<>(10);

    /**
     * 七牛云短信配置列表.
     */
    @Builder.Default
    private List<QiniuConfig> qiniu = new ArrayList<>(10);

    /**
     * 凌凯短信配置列表.
     */
    @Builder.Default
    private List<LingkaiConfig> lingkai = new ArrayList<>(10);

    /**
     * 麦讯通短信配置列表.
     */
    @Builder.Default
    private List<MxtongConfig> mxtong = new ArrayList<>(10);

    /**
     * SMS基础配置.
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
         * 配置编码（必填，用于标识不同的配置）.
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
     * 阿里云短信相关属性.
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
         * sts配置（可选）.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private StsTokenDTO sts = new StsTokenDTO();
    }

    /**
     * 七牛云短信相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    public static class QiniuConfig extends BaseConfig {

    }

    /**
     * 凌凯短信相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    public static class LingkaiConfig extends BaseConfig {

    }

    /**
     * 麦讯通短信相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    @SuperBuilder
    @NoArgsConstructor
    public static class MxtongConfig extends BaseConfig {

    }
}

