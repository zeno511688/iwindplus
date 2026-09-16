/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.address;

import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.address.domain.enums.AddressProviderEnum;
import com.iwindplus.base.address.domain.property.AddressProperty;
import com.iwindplus.base.address.factory.AddressExecuteHandlerFactory;
import com.iwindplus.base.address.support.AddressExecuteHandler;
import com.iwindplus.base.address.support.impl.BaiduAddressExecuteHandler;
import com.iwindplus.base.address.support.impl.GaodeAddressExecuteHandler;
import com.iwindplus.base.address.support.impl.Ip138AddressExecuteHandler;
import com.iwindplus.base.address.support.impl.PconlineAddressExecuteHandler;
import com.iwindplus.base.address.support.impl.TencentAddressExecuteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
public class AddressConfiguration {

    /**
     * 百度地图策略.
     *
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器策略工厂
     * @param property                        配置属性
     * @return 策略实例
     */
    @Bean
    public AddressExecuteHandler baiduAddressExecuteHandler(
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.BAIDU);
        if (config == null) {
            log.info("Baidu address provider is disabled");
            return null;
        }
        log.info("Initializing Baidu address strategy");
        return new BaiduAddressExecuteHandler(httpClientExecuteHandlerFactory, config);
    }

    /**
     * 高德地图策略.
     *
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器策略工厂
     * @param property                        配置属性
     * @return 策略实例
     */
    @Bean
    public AddressExecuteHandler gaodeAddressExecuteHandler(
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.GAODE);
        if (config == null) {
            log.info("Gaode address provider is disabled");
            return null;
        }
        log.info("Initializing Gaode address strategy");
        return new GaodeAddressExecuteHandler(httpClientExecuteHandlerFactory, config);
    }

    /**
     * 腾讯地图策略.
     *
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器策略工厂
     * @param property                        配置属性
     * @return 策略实例
     */
    @Bean
    public AddressExecuteHandler tencentAddressExecuteHandler(
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.TENCENT);
        if (config == null) {
            log.info("Tencent address provider is disabled");
            return null;
        }
        log.info("Initializing Tencent address strategy");
        return new TencentAddressExecuteHandler(httpClientExecuteHandlerFactory, config);
    }

    /**
     * IP138策略.
     *
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器策略工厂
     * @param property                        配置属性
     * @return 策略实例
     */
    @Bean
    public AddressExecuteHandler ip138AddressExecuteHandler(
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.IP138);
        if (config == null) {
            log.info("IP138 address provider is disabled");
            return null;
        }
        log.info("Initializing IP138 address strategy");
        return new Ip138AddressExecuteHandler(httpClientExecuteHandlerFactory, config);
    }

    /**
     * 太平洋网络策略.
     *
     * @param httpClientExecuteHandlerFactory HTTP客户端执行器策略工厂
     * @param property                        配置属性
     * @return 策略实例
     */
    @Bean
    public AddressExecuteHandler pconlineAddressExecuteHandler(
        HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory,
        AddressProperty property) {
        AddressProperty.ProviderConfig config = property.getProviderConfig(AddressProviderEnum.PCONLINE);
        if (config == null) {
            log.info("Pconline address provider is disabled");
            return null;
        }
        log.warn("Initializing Pconline address strategy (not recommended due to instability)");
        return new PconlineAddressExecuteHandler(httpClientExecuteHandlerFactory, config);
    }

    /**
     * 地址服务策略工厂.
     *
     * @param property         配置属性
     * @param executorProvider 执行器提供者
     * @return 策略工厂
     */
    @Bean
    public AddressExecuteHandlerFactory addressExecuteHandlerFactory(
        AddressProperty property,
        ObjectProvider<AddressExecuteHandler> executorProvider) {
        AddressExecuteHandlerFactory addressExecuteHandlerFactory = new AddressExecuteHandlerFactory(property, executorProvider);
        log.info("AddressExecuteHandlerFactory={}", addressExecuteHandlerFactory);
        return addressExecuteHandlerFactory;
    }
}
