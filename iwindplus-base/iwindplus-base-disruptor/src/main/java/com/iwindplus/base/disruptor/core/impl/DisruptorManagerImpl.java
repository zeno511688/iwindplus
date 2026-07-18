/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.core.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.disruptor.core.DisruptorManager;
import com.iwindplus.base.disruptor.domain.enums.DisruptorCodeEnum;
import com.iwindplus.base.disruptor.domain.enums.DisruptorWaitStrategyEnum;
import com.iwindplus.base.disruptor.domain.event.DisruptorEvent;
import com.iwindplus.base.disruptor.domain.property.DisruptorMultiProperty;
import com.iwindplus.base.disruptor.domain.property.DisruptorMultiProperty.DisruptorMultiConfig;
import com.iwindplus.base.disruptor.factory.DisruptorEventHandlerStrategyFactory;
import com.iwindplus.base.disruptor.support.DisruptorDispatcherHandler;
import com.iwindplus.base.disruptor.support.DisruptorEventHandler;
import com.iwindplus.base.disruptor.template.DisruptorTemplate;
import com.iwindplus.base.disruptor.template.impl.DefaultDisruptorTemplateImpl;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.LiteTimeoutBlockingWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.context.SmartLifecycle;

/**
 * 多Disruptor管理器实现类.
 *
 * @author zengdegui
 * @since 2026/03/21 22:01
 */
@Slf4j
@RequiredArgsConstructor
public class DisruptorManagerImpl<T> implements DisruptorManager<T>, SmartLifecycle {

    private final DisruptorMultiProperty property;
    private final DisruptorEventHandlerStrategyFactory factory;
    private final TraceContextPropagator traceContextPropagator;
    private final ObservationExecutor observationExecutor;

    private final Map<String, DisruptorTemplate<T>> templateMap = new ConcurrentHashMap<>(16);

    private volatile boolean running = false;

    @Override
    public void start() {

        init();

        running = true;

        log.info("DisruptorManager started");
    }

    @Override
    public void stop() {

        running = false;

        templateMap.clear();

        log.info("DisruptorManager stopped");
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public DisruptorTemplate<T> getTemplate(String name) {
        String targetName = resolveName(name);

        return Optional.ofNullable(
                templateMap.get(targetName)
            )
            .orElseThrow(() ->
                new BizException(
                    DisruptorCodeEnum.DISRUPTOR_CONFIG_NOT_EXIST
                ));
    }

    @Override
    public DisruptorMultiProperty getProperty() {
        return property;
    }

    /**
     * 初始化.
     */
    public void init() {
        if (Boolean.FALSE.equals(property.getEnabled())) {
            log.warn("Disruptor multi disabled");

            return;
        }

        final Map<String, DisruptorMultiConfig> configs =
            property.getConfigs();

        if (MapUtil.isEmpty(configs)) {
            throw new BizException(DisruptorCodeEnum.DISRUPTOR_CONFIG_NOT_EXIST);
        }

        buildTemplate(configs);

        log.info("Disruptor configs initialization completed. " +
                "configs={}, templates={}",
            property.getConfigs().size(),
            templateMap.size()
        );
    }

    private String resolveName(String name) {
        return CharSequenceUtil.isBlank(name)
            ? property.getDefaultName()
            : name;
    }

    private void buildTemplate(Map<String, DisruptorMultiConfig> configs) {
        Map<String, DisruptorEventHandler<?>> strategyMap = factory.getStrategyMap();
        // 判断是否为单配置模式（只有default配置）
        boolean singleMode = configs.size() == 1 && configs.containsKey(property.getDefaultName());
        // 单配置模式：所有处理器共享同一个Disruptor实例
        if (singleMode) {
            DisruptorMultiConfig defaultConfig = configs.get(property.getDefaultName());
            Disruptor<DisruptorEvent<?>> disruptor = createDisruptor(defaultConfig);
            // 所有处理器共享同一个Disruptor实例
            for (String handlerName : strategyMap.keySet()) {
                templateMap.put(handlerName, new DefaultDisruptorTemplateImpl(handlerName, disruptor, traceContextPropagator));
            }
            log.info("Disruptor single mode: all handlers share one disruptor instance, handlers={}", strategyMap.keySet());
        } else {
            // 多配置模式：每个处理器类型创建独立的Disruptor实例
            for (Map.Entry<String, DisruptorEventHandler<?>> entry : strategyMap.entrySet()) {
                String handlerName = entry.getKey();
                // 获取对应类型的配置，如果没有则使用默认配置
                DisruptorMultiConfig config = configs.getOrDefault(handlerName, configs.get(property.getDefaultName()));
                Disruptor<DisruptorEvent<?>> disruptor = createDisruptor(config);
                templateMap.put(handlerName, new DefaultDisruptorTemplateImpl(handlerName, disruptor, traceContextPropagator));
            }
            log.info("Disruptor multi mode: each handler has its own disruptor instance, handlers={}", strategyMap.keySet());
        }
    }

    /**
     * 创建Disruptor实例.
     *
     * @param config 配置
     * @return Disruptor实例
     */
    private Disruptor<DisruptorEvent<?>> createDisruptor(DisruptorMultiConfig config) {
        DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor(config.getThreadPoolName());
        Disruptor<DisruptorEvent<?>> disruptor = new Disruptor<>(
            DisruptorEvent::new,
            config.getRingBufferSize(),
            dtpExecutor.getThreadFactory(),
            config.getProducerType(),
            getWaitStrategy(config)
        );
        DisruptorDispatcherHandler dispatcherHandler = new DisruptorDispatcherHandler<>(factory, traceContextPropagator, observationExecutor);

        disruptor.handleEventsWith(dispatcherHandler);
        disruptor.start();
        return disruptor;
    }

    private WaitStrategy getWaitStrategy(DisruptorMultiConfig property) {
        final DisruptorWaitStrategyEnum waitStrategy = property.getWaitStrategy();
        return switch (property.getWaitStrategy()) {
            case BLOCKING -> new BlockingWaitStrategy();
            case LITE_BLOCKING -> new LiteBlockingWaitStrategy();
            case LITE_TIMEOUT_BLOCKING -> new LiteTimeoutBlockingWaitStrategy(property.getTimeout(), property.getTimeUnit());
            case TIMEOUT_BLOCKING -> new TimeoutBlockingWaitStrategy(property.getTimeout(), property.getTimeUnit());
            case SLEEPING -> new SleepingWaitStrategy();
            case YIELDING -> new YieldingWaitStrategy();
            case BUSY_SPIN -> new BusySpinWaitStrategy();
            default -> throw new IllegalArgumentException(
                "Unsupported wait strategy: " + waitStrategy
            );
        };
    }
}