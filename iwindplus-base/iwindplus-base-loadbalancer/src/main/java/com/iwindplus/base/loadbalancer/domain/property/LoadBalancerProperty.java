/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.domain.property;

import com.iwindplus.base.loadbalancer.domain.enums.GrayStrategyEnum;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 负载均衡配置属性.
 *
 * @author zengdegui
 * @since 2023/10/24 23:02
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "loadbalancer")
public class LoadBalancerProperty {

    /**
     * 监控配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private MonitorConfig monitor = new MonitorConfig();

    /**
     * 灰度发布配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private GrayConfig gray = new GrayConfig();

    /**
     * 监控配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonitorConfig {

        /**
         * 是否开启负载均衡监控指标.
         */
        @Builder.Default
        private Boolean enabled = false;
    }

    /**
     * 灰度发布配置.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrayConfig {

        /**
         * 是否开启.
         */
        @Builder.Default
        private Boolean enabled = false;

        /**
         * 灰度策略类型（whitelist: 白名单策略，percentage: 百分比策略）.
         */
        @Builder.Default
        private GrayStrategyEnum strategy = GrayStrategyEnum.PERCENTAGE;

        /**
         * 灰度用户ID白名单（白名单策略时使用）.
         */
        private List<String> userIdWhitelist;

        /**
         * 灰度百分比（0-100），默认10%（百分比策略时使用）.
         */
        @Builder.Default
        private Integer percentage = 10;
    }
}
