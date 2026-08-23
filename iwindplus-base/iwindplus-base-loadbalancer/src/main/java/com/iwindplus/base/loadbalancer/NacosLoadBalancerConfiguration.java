/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.iwindplus.base.loadbalancer.domain.property.LoadBalancerProperty;
import com.iwindplus.base.loadbalancer.support.NacosServiceInstanceLoadBalancer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Nacos负载均衡器配置（支持灰度发布）.
 *
 * @author zengdegui
 * @since 2023/10/24 23:02
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class NacosLoadBalancerConfiguration {

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Resource
    private LoadBalancerProperty loadBalancerProperty;

    /**
     * Nacos负载均衡器.
     *
     * @param environment               环境
     * @param loadBalancerClientFactory 负载均衡客户端工厂
     * @return ReactorLoadBalancer<ServiceInstance>
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> nacosLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        
        // 检查是否启用灰度发布
        LoadBalancerProperty.GrayConfig grayConfig = this.loadBalancerProperty.getGray();
        if (grayConfig.getEnabled() != null && grayConfig.getEnabled()) {
            // 使用灰度发布负载均衡器
            NacosServiceInstanceLoadBalancer loadBalancer = new NacosServiceInstanceLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), 
                name, 
                this.nacosDiscoveryProperties,
                grayConfig);
            log.info("NacosServiceInstanceLoadBalancer (with gray release) initialized for service: {}", name);
            return loadBalancer;
        } else {
            // 使用普通负载均衡器
            NacosServiceInstanceLoadBalancer loadBalancer = new NacosServiceInstanceLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), 
                name, 
                this.nacosDiscoveryProperties);
            log.info("NacosServiceInstanceLoadBalancer initialized for service: {}", name);
            return loadBalancer;
        }
    }
}
