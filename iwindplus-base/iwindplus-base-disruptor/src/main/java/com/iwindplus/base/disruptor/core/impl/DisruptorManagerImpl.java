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
import com.iwindplus.base.disruptor.domain.constant.DisruptorConstant.DisruptorMonitorConstant;
import com.iwindplus.base.disruptor.domain.enums.DisruptorCodeEnum;
import com.iwindplus.base.disruptor.domain.enums.DisruptorWaitStrategyEnum;
import com.iwindplus.base.disruptor.domain.event.DisruptorEvent;
import com.iwindplus.base.disruptor.domain.property.DisruptorMultiProperty;
import com.iwindplus.base.disruptor.domain.property.DisruptorMultiProperty.DisruptorMultiConfig;
import com.iwindplus.base.disruptor.factory.DisruptorEventHandlerStrategyFactory;
import com.iwindplus.base.disruptor.support.DisruptorDispatcherHandler;
import com.iwindplus.base.disruptor.template.DisruptorTemplate;
import com.iwindplus.base.disruptor.template.impl.DefaultDisruptorTemplateImpl;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.monitor.domain.constant.MonitorConstant;
import com.iwindplus.base.monitor.support.MonitorTemplate;
import com.iwindplus.base.monitor.support.ObservationExecutor;
import com.iwindplus.base.monitor.support.TraceContextPropagator;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.LiteTimeoutBlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import io.micrometer.core.instrument.Tags;
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
    private final MonitorTemplate monitorTemplate;

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

        configs.forEach((name, config) -> buildTemplate(name, config));

        log.info("Disruptor multi initialized: {}", configs.size());
    }

    private String resolveName(String name) {
        return CharSequenceUtil.isBlank(name)
            ? property.getDefaultName()
            : name;
    }

    private void buildTemplate(String name, DisruptorMultiConfig disruptorConfig) {
        // 如果配置不存在，则使用默认配置
        DisruptorMultiConfig config = Optional.ofNullable(disruptorConfig)
            .orElse(property.getConfigs().get(property.getDefaultName()));

        templateMap.computeIfAbsent(name, k -> {
            Disruptor<DisruptorEvent<?>> disruptor = createDisruptor(name, config);
            final DefaultDisruptorTemplateImpl disruptorTemplate = new DefaultDisruptorTemplateImpl(
                name, config, disruptor, traceContextPropagator);

            log.info("Disruptor template created, name={}", name);
            return disruptorTemplate;
        });
    }

    /**
     * 创建Disruptor实例.
     *
     * @param name   名称
     * @param config 配置
     * @return Disruptor实例
     */
    private Disruptor<DisruptorEvent<?>> createDisruptor(String name, DisruptorMultiConfig config) {
        DtpExecutor dtpExecutor = DtpRegistry.getDtpExecutor(config.getThreadPoolName());
        Disruptor<DisruptorEvent<?>> disruptor = new Disruptor<>(
            DisruptorEvent::new,
            config.getRingBufferSize(),
            dtpExecutor.getThreadFactory(),
            config.getProducerType(),
            getWaitStrategy(config)
        );
        DisruptorDispatcherHandler dispatcherHandler = new DisruptorDispatcherHandler<>(
            name, config, factory, traceContextPropagator, observationExecutor);

        disruptor.handleEventsWith(dispatcherHandler);
        disruptor.start();

        registerMonitor(name, disruptor.getRingBuffer());
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

    private void registerMonitor(String name, RingBuffer<DisruptorEvent<?>> ringBuffer) {
        if (Boolean.FALSE.equals(property.getEnabledMonitor())) {
            return;
        }

        Tags tags = Tags.of(MonitorConstant.NAME, name);
        // RingBuffer 总容量
        monitorTemplate.gauge(
            DisruptorMonitorConstant.RING_BUFFER_CAPACITY,
            tags,
            ringBuffer,
            RingBuffer::getBufferSize
        );

        // RingBuffer 剩余容量
        monitorTemplate.gauge(
            DisruptorMonitorConstant.RING_BUFFER_REMAINING,
            tags,
            ringBuffer,
            RingBuffer::remainingCapacity
        );
    }
}