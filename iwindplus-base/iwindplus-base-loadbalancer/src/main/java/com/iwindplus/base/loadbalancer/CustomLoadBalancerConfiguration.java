/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.iwindplus.base.loadbalancer.support.CustomServiceInstanceLoadBalancer;
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
 * 自定义负载均衡器配置.
 *
 * @author zengdegui
 * @since 2023/10/24 23:02
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class CustomLoadBalancerConfiguration {

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    /**
     * 自定义负载均衡器.
     *
     * @param environment               环境
     * @param loadBalancerClientFactory 负载均衡客户端工厂
     * @return ReactorLoadBalancer<ServiceInstance>
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> customLoadBalancer(Environment environment, LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        CustomServiceInstanceLoadBalancer loadBalancer = new CustomServiceInstanceLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name, this.nacosDiscoveryProperties);
        log.info(CustomServiceInstanceLoadBalancer.class.getSimpleName() + " initialized for service: {}", name);
        return loadBalancer;
    }
}
