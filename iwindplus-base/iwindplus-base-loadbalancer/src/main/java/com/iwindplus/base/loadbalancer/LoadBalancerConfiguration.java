/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.loadbalancer.strategy.CustomVersionWeightLoadBalancer;
import com.iwindplus.base.loadbalancer.strategy.NacosVersionWeightLoadBalancer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 自定义负载均衡器配置.
 *
 * @author zengdegui
 * @since 2023/10/24 23:02
 */
@Slf4j
public final class LoadBalancerConfiguration {

    private LoadBalancerConfiguration() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * nacos负载均衡启用配置.
     */
    @RefreshScope
    @ConditionalOnProperty(prefix = "loadbalancer.nacos", name = "enabled", havingValue = "true", matchIfMissing = true)
    @LoadBalancerClients(defaultConfiguration = {NacosVersionWeightLoadBalancerConfiguration.class})
    public static class LoadBalancerNacos {

    }

    /**
     * 自定义负载均衡启用配置.
     */
    @RefreshScope
    @ConditionalOnProperty(prefix = "loadbalancer.custom", name = "enabled", havingValue = "true")
    @LoadBalancerClients(defaultConfiguration = {CustomVersionWeightLoadBalancerConfiguration.class})
    public static class LoadBalancerCustom {

    }

    private static class NacosVersionWeightLoadBalancerConfiguration {

        @Resource
        private NacosDiscoveryProperties nacosDiscoveryProperties;

        /**
         * nacos负载均衡.
         *
         * @param environment               环境
         * @param loadBalancerClientFactory 负载均衡客户端工厂
         * @return ReactorLoadBalancer<ServiceInstance>
         */
        @Bean
        public ReactorLoadBalancer<ServiceInstance> nacosLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
            String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
            final NacosVersionWeightLoadBalancer loadBalancer = new NacosVersionWeightLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name, this.nacosDiscoveryProperties);
            log.info("NacosVersionWeightLoadBalancer={}", loadBalancer);
            return loadBalancer;
        }
    }

    private static class CustomVersionWeightLoadBalancerConfiguration {

        @Resource
        private NacosDiscoveryProperties nacosDiscoveryProperties;

        /**
         * 自定义负载均衡.
         *
         * @param environment               环境
         * @param loadBalancerClientFactory 负载均衡客户端工厂
         * @return ReactorLoadBalancer<ServiceInstance>
         */
        @Bean
        public ReactorLoadBalancer<ServiceInstance> customLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
            String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
            final CustomVersionWeightLoadBalancer loadBalancer = new CustomVersionWeightLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name, nacosDiscoveryProperties);
            log.info("CustomVersionWeightLoadBalancer={}", loadBalancer);
            return loadBalancer;
        }
    }
}
