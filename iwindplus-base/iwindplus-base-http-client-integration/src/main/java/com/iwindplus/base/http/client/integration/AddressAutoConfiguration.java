/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration;

import com.iwindplus.base.http.client.factory.HttpClientExecutorStrategyFactory;
import com.iwindplus.base.http.client.integration.domain.enums.AddressProviderEnum;
import com.iwindplus.base.http.client.integration.domain.property.AddressProperty;
import com.iwindplus.base.http.client.integration.factory.AddressExecuteHandlerStrategyFactory;
import com.iwindplus.base.http.client.integration.service.address.AddressService;
import com.iwindplus.base.http.client.integration.service.address.impl.AddressServiceImpl;
import com.iwindplus.base.http.client.integration.support.adress.AddressExecuteHandler;
import com.iwindplus.base.http.client.integration.support.adress.impl.BaiduAddressExecuteHandler;
import com.iwindplus.base.http.client.integration.support.adress.impl.GaodeAddressExecuteHandler;
import com.iwindplus.base.http.client.integration.support.adress.impl.Ip138AddressExecuteHandler;
import com.iwindplus.base.http.client.integration.support.adress.impl.PconlineAddressExecuteHandler;
import com.iwindplus.base.http.client.integration.support.adress.impl.TencentAddressExecuteHandler;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 地址服务自动配置.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AddressProperty.class)
@ConditionalOnProperty(prefix = "address", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AddressAutoConfiguration {

    /**
     * 百度地图策略.
     *
     * @param httpClientExecutorStrategyFactory HTTP客户端执行器策略工厂
     * @param property                          配置属性
     * @return 策略实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "baiduAddressStrategy")
    public AddressExecuteHandler baiduAddressStrategy(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.BAIDU);
        if (config == null) {
            log.info("Baidu address provider is disabled");
            return null;
        }
        log.info("Initializing Baidu address strategy");
        return new BaiduAddressExecuteHandler(httpClientExecutorStrategyFactory, config);
    }

    /**
     * 高德地图策略.
     *
     * @param httpClientExecutorStrategyFactory HTTP客户端执行器策略工厂
     * @param property                          配置属性
     * @return 策略实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "gaodeAddressStrategy")
    public AddressExecuteHandler gaodeAddressStrategy(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.GAODE);
        if (config == null) {
            log.info("Gaode address provider is disabled");
            return null;
        }
        log.info("Initializing Gaode address strategy");
        return new GaodeAddressExecuteHandler(httpClientExecutorStrategyFactory, config);
    }

    /**
     * 腾讯地图策略.
     *
     * @param httpClientExecutorStrategyFactory HTTP客户端执行器策略工厂
     * @param property                          配置属性
     * @return 策略实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "tencentAddressStrategy")
    public AddressExecuteHandler tencentAddressStrategy(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.TENCENT);
        if (config == null) {
            log.info("Tencent address provider is disabled");
            return null;
        }
        log.info("Initializing Tencent address strategy");
        return new TencentAddressExecuteHandler(httpClientExecutorStrategyFactory, config);
    }

    /**
     * IP138策略.
     *
     * @param httpClientExecutorStrategyFactory HTTP客户端执行器策略工厂
     * @param property                          配置属性
     * @return 策略实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "ip138AddressStrategy")
    public AddressExecuteHandler ip138AddressStrategy(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.IP138);
        if (config == null) {
            log.info("IP138 address provider is disabled");
            return null;
        }
        log.info("Initializing IP138 address strategy");
        return new Ip138AddressExecuteHandler(httpClientExecutorStrategyFactory, config);
    }

    /**
     * 太平洋网络策略.
     *
     * @param httpClientExecutorStrategyFactory HTTP客户端执行器策略工厂
     * @param property                          配置属性
     * @return 策略实例
     */
    @Bean
    @ConditionalOnMissingBean(name = "pconlineAddressStrategy")
    public AddressExecuteHandler pconlineAddressStrategy(
        HttpClientExecutorStrategyFactory httpClientExecutorStrategyFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.PCONLINE);
        if (config == null) {
            log.info("Pconline address provider is disabled");
            return null;
        }
        log.warn("Initializing Pconline address strategy (not recommended due to instability)");
        return new PconlineAddressExecuteHandler(httpClientExecutorStrategyFactory, config);
    }

    /**
     * 地址服务策略工厂.
     *
     * @param property   配置属性
     * @param strategies 策略列表
     * @return 策略工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public AddressExecuteHandlerStrategyFactory addressExecuteHandlerStrategyFactory(
        AddressProperty property,
        List<AddressExecuteHandler> strategies) {
        // 过滤掉 null 的策略
        List<AddressExecuteHandler> validStrategies = strategies.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        log.info("Initializing AddressExecuteHandlerStrategyFactory with {} strategies", validStrategies.size());
        return new AddressExecuteHandlerStrategyFactory(property, validStrategies);
    }

    /**
     * 地址服务.
     *
     * @param addressExecuteHandlerStrategyFactory 地址服务策略工厂
     * @return 地址服务
     */
    @Bean
    @ConditionalOnMissingBean
    public AddressService addressService(
        AddressExecuteHandlerStrategyFactory addressExecuteHandlerStrategyFactory) {
        log.info("Initializing AddressService");
        return new AddressServiceImpl(addressExecuteHandlerStrategyFactory);
    }
}
