/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.address.factory;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.address.domain.enums.AddressProviderEnum;
import com.iwindplus.base.address.domain.property.AddressProperty;
import com.iwindplus.base.address.domain.vo.AddressVO;
import com.iwindplus.base.address.support.AddressExecuteHandler;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;

/**
 * 地址服务策略工厂. 负责管理和路由地址服务策略，支持动态路由和故障转移.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class AddressExecuteHandlerFactory implements SmartInitializingSingleton {

    private final AddressProperty property;
    private final Supplier<Map<AddressProviderEnum, AddressExecuteHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         属性配置
     * @param executorProvider 执行器提供者
     */
    public AddressExecuteHandlerFactory(AddressProperty property, ObjectProvider<AddressExecuteHandler> executorProvider) {
        this.property = property;
        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<AddressProviderEnum, AddressExecuteHandler>
                strategyMap = executorProvider
                .orderedStream()
                .collect(Collectors.toMap(
                    AddressExecuteHandler::getProvider,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                AddressExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 查询地址信息（自动故障转移）.
     *
     * @param ip IP地址
     * @return 地址信息
     */
    public Optional<AddressVO> getAddress(String ip) {
        // 前置检查
        if (!checkPreconditions(ip)) {
            return Optional.empty();
        }

        // 获取启用的提供商配置（按优先级排序）
        Map<String, AddressProperty.ProviderConfig> enabledProviders = this.property.getEnabledProviders();

        if (enabledProviders.isEmpty()) {
            log.warn("No enabled address providers configured");
            return Optional.empty();
        }

        // 按优先级依次尝试
        for (Map.Entry<String, AddressProperty.ProviderConfig> entry : enabledProviders.entrySet()) {
            String providerCode = entry.getKey();
            AddressProviderEnum provider = AddressProviderEnum.getByCode(providerCode);
            if (provider == null) {
                log.warn("Invalid provider code: {}", providerCode);
                continue;
            }

            Optional<AddressVO> result = queryByProvider(ip, provider);
            if (result.isPresent()) {
                return result;
            }
        }

        log.error("All address providers failed for ip={}", ip);
        return Optional.empty();
    }

    /**
     * 使用指定提供商查询地址信息.
     *
     * @param ip       IP地址
     * @param provider 提供商
     * @return 地址信息
     */
    public Optional<AddressVO> getAddress(String ip, AddressProviderEnum provider) {
        // 前置检查
        if (!checkPreconditions(ip)) {
            return Optional.empty();
        }

        return queryByProvider(ip, provider);
    }

    /**
     * 使用指定提供商查询地址信息（内部方法）.
     *
     * @param ip       IP地址
     * @param provider 提供商
     * @return 地址信息
     */
    private Optional<AddressVO> queryByProvider(String ip, AddressProviderEnum provider) {
        AddressExecuteHandler strategy = this.getStrategyMap().get(provider);
        if (strategy == null) {
            log.warn("No strategy found for provider: {}", provider.getName());
            return Optional.empty();
        }

        // 健康检查
        if (!strategy.isHealthy()) {
            log.warn("Provider {} is not healthy, skipping", provider.getName());
            return Optional.empty();
        }

        // 尝试查询
        Optional<AddressVO> result = strategy.getAddress(ip);
        if (result.isPresent()) {
            log.info("Address query succeeded [provider={}, ip={}]", provider.getName(), ip);
        } else {
            log.warn("Address query failed [provider={}, ip={}]", provider.getName(), ip);
        }

        return result;
    }

    /**
     * 获取所有可用的提供商.
     *
     * @return 提供商列表
     */
    public List<AddressProviderEnum> getAvailableProviders() {
        return this.getStrategyMap().values().stream()
            .filter(AddressExecuteHandler::isHealthy)
            .sorted(Comparator.comparingInt(AddressExecuteHandler::getPriority))
            .map(AddressExecuteHandler::getProvider)
            .collect(Collectors.toList());
    }

    /**
     * 获取提供商配置.
     *
     * @param provider 提供商
     * @return 配置
     */
    public AddressProperty.ProviderConfig getProviderConfig(AddressProviderEnum provider) {
        return this.property.getProviderConfig(provider);
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<AddressProviderEnum, AddressExecuteHandler>
     */
    private Map<AddressProviderEnum, AddressExecuteHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

    /**
     * 前置检查（服务是否启用、IP是否有效）.
     *
     * @param ip IP地址
     * @return 是否通过检查
     */
    private boolean checkPreconditions(String ip) {
        if (!Boolean.TRUE.equals(this.property.getEnabled())) {
            log.warn("Address service is disabled");
            return false;
        }

        // 检查是否为本地回环地址或私有IP地址
        if (isPrivateIp(ip)) {
            log.debug("Skip private IP query: ip={}", ip);
            return false;
        }

        return true;
    }

    /**
     * 检查是否为本地回环地址或私有IP地址.
     *
     * @param ip IP地址
     * @return 是否为私有IP地址
     */
    private boolean isPrivateIp(String ip) {
        if (CharSequenceUtil.isBlank(ip)) {
            return false;
        }

        // 使用 Hutool 的 NetUtil.isInnerIP() 方法判断是否为内网IP
        // 该方法会自动判断以下类型的IP：
        // 1. 本地回环地址：127.0.0.0 - 127.255.255.255
        // 2. A类私有地址：10.0.0.0 - 10.255.255.255
        // 3. B类私有地址：172.16.0.0 - 172.31.255.255
        // 4. C类私有地址：192.168.0.0 - 192.168.255.255
        // 5. 链路本地地址：169.254.0.0 - 169.254.255.255
        return NetUtil.isInnerIP(ip);
    }
}
