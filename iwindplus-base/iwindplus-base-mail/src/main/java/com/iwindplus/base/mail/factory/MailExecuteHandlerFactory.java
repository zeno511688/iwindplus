/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.factory;

import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.mail.domain.dto.MailDTO;
import com.iwindplus.base.mail.domain.property.MailProperty;
import com.iwindplus.base.mail.domain.vo.MailVO;
import com.iwindplus.base.mail.support.MailExecuteHandler;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.function.SingletonSupplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 邮件策略工厂. 负责管理和路由邮件策略，支持多配置、动态路由.
 *
 * @author zengdegui
 * @since 2024/12/26
 */
@Slf4j
public class MailExecuteHandlerFactory implements SmartInitializingSingleton {

    private final MailProperty property;
    private final Supplier<Map<String, MailExecuteHandler>> strategyMapSupplier;

    /**
     * 构造函数.
     *
     * @param property         属性配置
     * @param executorHandlers 执行器列表
     */
    public MailExecuteHandlerFactory(MailProperty property, List<MailExecuteHandler> executorHandlers) {
        this.property = property;
        this.strategyMapSupplier = SingletonSupplier.of(() -> {

            final Map<String, MailExecuteHandler>
                strategyMap = executorHandlers
                .stream()
                .collect(Collectors.toMap(
                    MailExecuteHandler::getCode,
                    Function.identity(),
                    (existing, replacement) -> replacement
                ));

            log.info("Loaded {} strategies={}",
                MailExecuteHandler.class.getSimpleName(),
                strategyMap.keySet()
            );

            return strategyMap;
        });
    }

    /**
     * 根据邮件类型获取邮件策略（该提供商下优先级最高的可用策略）.
     *
     * @param code 邮件类型
     * @return 邮件策略
     */
    public MailExecuteHandler getHandler(String code) {
        final MailExecuteHandler handler = getStrategyMap().get(code);
        if (handler == null) {
            log.error("MailHandler invalid strategy={}", code);
            throw new BizException(BizCodeEnum.INVALID_STRATEGY);
        }
        return handler;
    }

    /**
     * 获取默认邮件策略.
     *
     * @return 默认邮件策略
     */
    public MailExecuteHandler getDefaultHandler() {
        String defaultCode = this.property.getDefaultCode();
        if (defaultCode == null || defaultCode.isBlank()) {
            log.warn("MailExecuteHandler default-code not configured");
            return null;
        }
        return getStrategyMap().values().stream()
            .filter(handler -> handler.getCode().equals(defaultCode) && handler.isHealthy())
            .findFirst()
            .orElseGet(() -> {
                log.error("MailExecuteHandler default-code invalid code={}", defaultCode);
                return null;
            });
    }

    /**
     * 根据配置编码获取邮件配置.
     *
     * @param code 配置编码
     * @return 邮件配置
     */
    public MailProperty.MailConfig getConfig(String code) {
        return this.property.getConfigs().stream()
            .filter(config -> code.equals(config.getCode()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 发送邮件（轮询式故障转移 + 重试）.
     *
     * <p>每一轮依次尝试所有配置，某个配置发送失败则立刻切换下一个配置；
     * 所有配置都失败后进入下一轮重试，直到 {@code maxAttempts} 轮耗尽或某个配置发送成功。</p>
     *
     * <p>重试行为由全局配置控制：{@code enableRetry} 关闭时只做一轮故障转移；
     * 开启时每轮之间按 {@code period} 到 {@code maxPeriod} 指数退避延迟。</p>
     *
     * @param entity 邮件发送参数
     * @return 邮件发送结果，全部失败时返回 {@link Mono#empty()}
     */
    public Mono<MailVO> send(MailDTO entity) {
        final List<MailExecuteHandler> handlers = getAvailableHandlers();
        if (handlers.isEmpty()) {
            log.warn("No available mail handlers for send");
            return Mono.empty();
        }

        // 是否开启重试（默认开启）
        final boolean enableRetry = Optional.ofNullable(this.property.getEnableRetry()).orElse(Boolean.TRUE);
        // 总轮次：开启重试时为 maxAttempts，否则只做一轮故障转移
        final int maxAttempts = enableRetry
            ? Optional.ofNullable(this.property.getMaxAttempts()).orElse(5)
            : 1;

        return Flux.range(1, maxAttempts)
            .concatMap(attempt -> {
                final Duration backoff = attempt == 1
                    ? Duration.ZERO
                    : this.calculateBackoff(attempt);

                if (!backoff.isZero()) {
                    log.warn("Mail send retry [attempt={}, retryCount={}, backoff={}]",
                        attempt,
                        attempt - 1,
                        backoff);
                }

                return this.sendRound(entity, handlers, attempt)
                    .delaySubscription(backoff);
            })
            .next()
            .switchIfEmpty(Mono.defer(() -> {
                log.error("Mail send failed after all attempts [maxAttempts={}]",
                    maxAttempts);
                return Mono.empty();
            }));
    }

    /**
     * 单轮故障转移：依次尝试所有配置，某个配置成功则返回，全部失败则返回空.
     *
     * @param entity   邮件发送参数
     * @param handlers 可用策略列表
     * @param attempt  当前轮次
     * @return 发送结果
     */
    private Mono<MailVO> sendRound(
        MailDTO entity,
        List<MailExecuteHandler> handlers,
        int attempt) {

        return Flux.fromIterable(handlers)
            .concatMap(handler -> handler.send(entity)
                .doOnNext(result -> {
                    if (Boolean.TRUE.equals(result.getResult())) {
                        log.info("Mail sent successfully [code={}, attempt={}]",
                            handler.getCode(), attempt);
                    } else {
                        log.warn("Mail send failed, trying next handler [code={}, attempt={}]",
                            handler.getCode(), attempt);
                    }
                })
                .filter(result -> Boolean.TRUE.equals(result.getResult()))
                .onErrorResume(ex -> {
                    log.warn("Mail handler failed, trying next handler [code={}, attempt={}]",
                        handler.getCode(), attempt, ex);
                    return Mono.empty();
                }))
            .next();
    }

    /**
     * 计算第 N 轮（N &gt; 1）的重试退避延迟：{@code min(period * 2^(N-2), maxPeriod)}.
     *
     * @param attempt 当前轮次
     * @return 退避延迟
     */
    private Duration calculateBackoff(int attempt) {
        final Duration period = Optional.ofNullable(this.property.getPeriod()).orElse(Duration.ofSeconds(5));
        final Duration maxPeriod = Optional.ofNullable(this.property.getMaxPeriod()).orElse(Duration.ofSeconds(3600));
        final long periodMillis = period.toMillis();
        final long maxMillis = maxPeriod.toMillis();
        // 限制位移上限，避免 1L << exponent 溢出（exponent 超过 62 会溢出为负数）
        final int exponent = Math.min(Math.max(0, attempt - 2), 62);
        final long backoffMillis = periodMillis * (1L << exponent);
        return Duration.ofMillis(Math.min(backoffMillis, maxMillis));
    }

    /**
     * 获取所有可用（健康）的邮件策略，按优先级排序
     *
     * @return 可用策略列表
     */
    public List<MailExecuteHandler> getAvailableHandlers() {
        final boolean enabledFailover =
            Optional.ofNullable(property.getEnabledFailover()).orElse(Boolean.TRUE);

        final List<MailExecuteHandler> healthyHandlers = getStrategyMap().values().stream()
            .filter(MailExecuteHandler::isHealthy)
            .sorted(Comparator.comparingInt(MailExecuteHandler::getPriority))
            .toList();

        if (enabledFailover) {
            // 启用故障转移：所有健康配置按照优先级参与
            return healthyHandlers;
        }

        // 未启用故障转移：只使用优先级最高的配置
        return healthyHandlers.stream()
            .findFirst()
            .map(List::of)
            .orElseGet(List::of);
    }

    @Override
    public void afterSingletonsInstantiated() {
        getStrategyMap();
    }

    /**
     * 获取策略缓存.
     *
     * @return Map<String, MailExecuteHandler>
     */
    private Map<String, MailExecuteHandler> getStrategyMap() {
        return strategyMapSupplier.get();
    }

}
