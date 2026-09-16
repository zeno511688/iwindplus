/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 即时通讯服务配置相关属性配置相关属性.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "im")
public class ImProperty {

    /**
     * 是否启用远程token校验.
     */
    @Builder.Default
    private Boolean enabledRemoteToken = Boolean.FALSE;

    /**
     * 聊天群扫码加入地址.
     */
    private String chatGroupScanUrl;

    /**
     * 对象存储配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private OssConfig oss = new OssConfig();

    /**
     * 对象存储相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OssConfig {

        /**
         * 对象存储配置编码.
         */
        private String code;

        /**
         * 对象存储模板编码.
         */
        private String tplCode;
    }
}
