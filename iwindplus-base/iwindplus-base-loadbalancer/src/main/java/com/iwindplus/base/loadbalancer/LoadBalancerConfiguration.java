/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer;

import com.iwindplus.base.loadbalancer.domain.property.LoadBalancerProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Configuration;

/**
 * 负载均衡器自动配置.
 *
 * @author zengdegui
 * @since 2023/10/24 23:02
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LoadBalancerProperty.class)
public class LoadBalancerConfiguration {

    /**
     * Nacos负载均衡自动配置（支持灰度发布）.
     */
    @Configuration(proxyBeanMethods = false)
    @LoadBalancerClients(defaultConfiguration = NacosLoadBalancerConfiguration.class)
    public static class NacosLoadBalancerAutoConfiguration {

    }
}
