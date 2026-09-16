/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms.factory;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.enums.SmsTypeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.sms.domain.dto.SmsSendCaptchaDTO;
import com.iwindplus.base.sms.domain.dto.SmsSendRequestDTO;
import com.iwindplus.base.sms.domain.property.SmsProperty;
import com.iwindplus.base.sms.domain.vo.SmsSendBatchResultVO;
import com.iwindplus.base.sms.domain.vo.SmsSendResultVO;
import com.iwindplus.base.sms.support.SmsExecuteHandler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;

/**
 * 短信策略工厂.
 * 负责管理和路由短信策略，支持多配置、动态路由和自动故障转移.
 *
 * <p>策略结构：{@code Map<SmsTypeEnum, Map<String, SmsExecuteHandler>>}，
 * 第一维为提供商类型，第二维为配置编码，同一提供商可存在多个配置。</p>
 *
 * @author zengdegui
 * @since 2024/12/26
 */
@Slf4j
public class SmsExecuteHandlerFactory implements SmartInitializingSingleton {

    private final SmsProperty property;
    private final Supplier<Map<SmsTypeEnum, Map<String, SmsExecuteHandler>>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         短信属性配置
     * @param executorHandlers 执行器列表
     */
    public SmsExecuteHandlerFactory(SmsProperty property, List<SmsExecuteHandler> executorHandlers) {
        this.property = property;
        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<SmsTypeEnum, Map<String, SmsExecuteHandler>>
                strategyMap = executorHandlers
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                    SmsExecuteHandler::getProvider,
                    LinkedHashMap::new,
                    Collectors.toMap(
                        SmsExecuteHandler::getCode,
                        Function.identity(),
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                    )
                ));

            log.info("Loaded {} strategies={}",
                SmsExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 根据短信类型获取短信策略（该提供商下优先级最高的可用策略）.
     *
     * @param type 短信类型
     * @return 短信策略
     */
    public SmsExecuteHandler getHandler(SmsTypeEnum type) {
        Map<String, SmsExecuteHandler> handlers = getStrategyMap().get(type);
        if (handlers == null || handlers.isEmpty()) {
            log.error("SmsExecuteHandler invalid strategy={}", type);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handlers.values().stream()
            .filter(SmsExecuteHandler::isHealthy)
            .min(Comparator.comparingInt(SmsExecuteHandler::getPriority))
            .orElseThrow(() -> {
                log.error("SmsExecuteHandler no available strategy={}", type);
                return new BizException(BizCodeEnum.INVALID_STRATEGY);
            });
    }

    /**
     * 根据短信类型和配置编码获取短信策略.
     *
     * @param type 短信类型
     * @param code 配置编码
     * @return 短信策略
     */
    public SmsExecuteHandler getHandler(SmsTypeEnum type, String code) {
        Map<String, SmsExecuteHandler> handlers = getStrategyMap().get(type);
        if (handlers == null) {
            log.error("SmsExecuteHandler invalid strategy={}", type);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        SmsExecuteHandler handler = handlers.get(code);
        if (handler == null) {
            log.error("SmsExecuteHandler invalid code={}", code);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 根据短信类型和配置编码获取短信配置.
     *
     * @param type 短信类型
     * @param code 配置编码
     * @return 短信配置
     */
    public SmsProperty.BaseConfig getConfig(SmsTypeEnum type, String code) {
        final List<? extends SmsProperty.BaseConfig> configs = switch (type) {
            case ALIYUN -> this.property.getAliyun();
            case QINIU -> this.property.getQiniu();
            case LINGKAI -> this.property.getLingkai();
            case MXTONG -> this.property.getMxtong();
        };

        if (CollUtil.isEmpty(configs)) {
            return null;
        }
        return configs.stream()
            .filter(config -> code.equals(config.getCode()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 发送短信验证码（自动故障转移）.
     *
     * <p>故障转移策略：优先切换提供商（同一提供商多配置共享基础设施，一个出问题其他大概率也出问题），
     * 再在同一提供商内按配置优先级切换。</p>
     *
     * @param entity 短信验证码发送参数
     * @return 短信日志，全部失败时返回 {@link Optional#empty()}
     */
    public Optional<SmsSendResultVO> smsSendCaptcha(SmsSendCaptchaDTO entity) {
        List<SmsExecuteHandler> handlers = getAvailableHandlers();
        if (handlers.isEmpty()) {
            log.warn("No available SMS handlers for captcha");
            return Optional.empty();
        }
        for (SmsExecuteHandler handler : handlers) {
            Optional<SmsSendResultVO> result = handler.smsSendCaptcha(entity);
            if (result.isPresent()) {
                log.info("SMS captcha sent successfully [provider={}, code={}]",
                    handler.getProvider().getDesc(), handler.getCode());
                return result;
            }
            log.warn("SMS captcha send failed, trying next handler [provider={}, code={}]",
                handler.getProvider().getDesc(), handler.getCode());
        }
        log.error("All SMS handlers failed for captcha");
        return Optional.empty();
    }

    /**
     * 发送短信（自动故障转移）.
     *
     * <p>故障转移策略：优先切换提供商，再在同一提供商内按配置优先级切换。</p>
     *
     * @param entity 短信发送参数
     * @return 批量发送结果，全部失败时返回 {@link Optional#empty()}
     */
    public Optional<List<SmsSendBatchResultVO>> smsSend(SmsSendRequestDTO entity) {
        List<SmsExecuteHandler> handlers = getAvailableHandlers();
        if (handlers.isEmpty()) {
            log.warn("No available SMS handlers for send");
            return Optional.empty();
        }
        for (SmsExecuteHandler handler : handlers) {
            Optional<List<SmsSendBatchResultVO>> result = handler.smsSend(entity);
            if (result.isPresent()) {
                log.info("SMS sent successfully [provider={}, code={}]",
                    handler.getProvider().getDesc(), handler.getCode());
                return result;
            }
            log.warn("SMS send failed, trying next handler [provider={}, code={}]",
                handler.getProvider().getDesc(), handler.getCode());
        }
        log.error("All SMS handlers failed for send");
        return Optional.empty();
    }

    /**
     * 获取所有可用（健康）的短信策略，按优先级排序.
     *
     * <p>排序规则：先按提供商优先级排序，再按配置优先级排序。
     * 若未启用故障转移，则每个提供商只返回优先级最高的一个配置。</p>
     *
     * @return 可用策略列表
     */
    public List<SmsExecuteHandler> getAvailableHandlers() {
        boolean enabledFailover = Optional.ofNullable(property.getEnabledFailover()).orElse(Boolean.TRUE);
        List<SmsExecuteHandler> handlers = new ArrayList<>(10);
        // 按提供商优先级排序
        List<Map.Entry<SmsTypeEnum, Map<String, SmsExecuteHandler>>> providerEntries = getStrategyMap().entrySet().stream()
            .sorted(Comparator.comparingInt(entry -> entry.getValue().values().stream()
                .filter(SmsExecuteHandler::isHealthy)
                .mapToInt(SmsExecuteHandler::getPriority)
                .min()
                .orElse(Integer.MAX_VALUE)))
            .collect(Collectors.toList());
        for (Map.Entry<SmsTypeEnum, Map<String, SmsExecuteHandler>> entry : providerEntries) {
            List<SmsExecuteHandler> healthyHandlers = entry.getValue().values().stream()
                .filter(SmsExecuteHandler::isHealthy)
                .sorted(Comparator.comparingInt(SmsExecuteHandler::getPriority))
                .toList();
            if (healthyHandlers.isEmpty()) {
                continue;
            }
            if (enabledFailover) {
                // 启用故障转移：同一提供商的所有健康配置都参与
                handlers.addAll(healthyHandlers);
            } else {
                // 未启用故障转移：每个提供商只取优先级最高的一个配置
                handlers.add(healthyHandlers.get(0));
            }
        }
        return handlers;
    }

    /**
     * 获取所有可用的短信类型.
     *
     * @return 短信类型列表
     */
    public List<SmsTypeEnum> getAvailableProviders() {
        return getAvailableHandlers().stream()
            .map(SmsExecuteHandler::getProvider)
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<SmsTypeEnum, Map<String, SmsExecuteHandler>>
     */
    private Map<SmsTypeEnum, Map<String, SmsExecuteHandler>> getStrategyMap() {
        return strategyMapSupplier.get();
    }

}
