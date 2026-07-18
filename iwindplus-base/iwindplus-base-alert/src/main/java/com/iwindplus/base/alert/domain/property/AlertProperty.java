/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.domain.property;

import com.iwindplus.base.alert.domain.enums.AlertChannelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 告警相关属性.
 *
 * @author zengdegui
 * @since 2025/11/23 21:18
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "alert")
public class AlertProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 默认告警渠道.
     */
    @Builder.Default
    private AlertChannelTypeEnum defaultAlertChannel = AlertChannelTypeEnum.FEI_SHU;

    /**
     * 飞书配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private FeishuConfig feishu = new FeishuConfig();

    /**
     * 飞书相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeishuConfig {

        /**
         * 应用主键.
         */
        private String appId;

        /**
         * 应用密钥.
         */
        private String appSecret;
    }
}
