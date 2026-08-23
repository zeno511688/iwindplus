/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.domain.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 负载均衡配置属性.
 *
 * @author zengdegui
 * @since 2023/10/24 23:02
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "loadbalancer")
public class LoadBalancerProperty {

    /**
     * Nacos负载均衡配置.
     */
    private NacosConfig nacos = new NacosConfig();

    /**
     * 自定义负载均衡配置.
     */
    private CustomConfig custom = new CustomConfig();

    @Data
    public static class NacosConfig {

        /**
         * 是否启用Nacos负载均衡，默认true.
         */
        private Boolean enabled = true;
    }

    @Data
    public static class CustomConfig {

        /**
         * 是否启用自定义负载均衡，默认false.
         */
        private Boolean enabled = false;
    }
}
