/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.factory;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.http.client.integration.domain.enums.AddressProviderEnum;
import com.iwindplus.base.http.client.integration.domain.property.AddressProperty;
import com.iwindplus.base.http.client.integration.domain.vo.address.AddressVO;
import com.iwindplus.base.http.client.integration.support.adress.AddressExecuteHandler;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * 地址服务策略工厂.
 * 负责管理和路由地址服务策略，支持动态路由和故障转移.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Slf4j
public class AddressExecuteHandlerStrategyFactory {

    private final AddressProperty property;
    private final Map<AddressProviderEnum, AddressExecuteHandler> strategyMap = new ConcurrentHashMap<>();

    public AddressExecuteHandlerStrategyFactory(AddressProperty property, List<AddressExecuteHandler> strategies) {
        this.property = property;
        // 初始化策略映射
        strategies.forEach(strategy -> strategyMap.put(strategy.getProvider(), strategy));
    }

    /**
     * 查询地址信息（自动故障转移）.
     *
     * @param ip IP地址
     * @return 地址信息
     */
    public Optional<AddressVO> queryAddress(String ip) {
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
     * @param ip IP地址
     * @param provider 提供商
     * @return 地址信息
     */
    public Optional<AddressVO> queryAddress(String ip, AddressProviderEnum provider) {
        // 前置检查
        if (!checkPreconditions(ip)) {
            return Optional.empty();
        }

        return queryByProvider(ip, provider);
    }

    /**
     * 使用指定提供商查询地址信息（内部方法）.
     *
     * @param ip IP地址
     * @param provider 提供商
     * @return 地址信息
     */
    private Optional<AddressVO> queryByProvider(String ip, AddressProviderEnum provider) {
        AddressExecuteHandler strategy = this.strategyMap.get(provider);
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
        Optional<AddressVO> result = strategy.queryAddress(ip);
        if (result.isPresent()) {
            log.info("Address query succeeded [provider={}, ip={}]", provider.getName(), ip);
        } else {
            log.warn("Address query failed [provider={}, ip={}]", provider.getName(), ip);
        }

        return result;
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
     * 获取所有可用的提供商.
     *
     * @return 提供商列表
     */
    public List<AddressProviderEnum> getAvailableProviders() {
        return this.strategyMap.values().stream()
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
