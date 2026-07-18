/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.domain.property;

import com.iwindplus.base.disruptor.domain.enums.DisruptorWaitStrategyEnum;
import com.iwindplus.base.domain.constant.CommonConstant.SystemConstant;
import com.lmax.disruptor.dsl.ProducerType;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 多disruptor配置.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Slf4j
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "disruptor.multi")
public class DisruptorMultiProperty {

    /**
     * 是否开启.
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 默认Disruptor名称（必填）.
     */
    @Builder.Default
    private String defaultName = SystemConstant.DEFAULT;

    /**
     * 多Disruptor配置，key为处理器类型，value为对应的配置.
     */
    @NestedConfigurationProperty
    private Map<String, DisruptorMultiConfig> configs;

    /**
     * 多disruptor配置对象.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisruptorMultiConfig {

        /**
         * 环形缓冲区大小.
         */
        @Builder.Default
        private int ringBufferSize = 8192;

        /**
         * 生产者类型.
         */
        @Builder.Default
        private ProducerType producerType = ProducerType.SINGLE;

        /**
         * 等待策略.
         */
        @Builder.Default
        private DisruptorWaitStrategyEnum waitStrategy = DisruptorWaitStrategyEnum.YIELDING;

        /**
         * 等待超时时间.
         */
        @Builder.Default
        private Long timeout = 100L;

        /**
         * 阻塞等待超时单位.
         */
        @Builder.Default
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        /**
         * 线程池bean名称（对于DtpExecutor）.
         */
        @Builder.Default
        private String threadPoolName = "disruptorTaskExecutor";
    }

    /**
     * 初始化默认配置. 如果configs中不存在default配置，则自动添加一个默认配置.
     */
    @PostConstruct
    public void initDefaultConfig() {
        if (configs == null) {
            configs = new HashMap<>(16);
        }

        configs.computeIfAbsent(this.defaultName, value -> {
            log.info("未配置 default Disruptor 配置，自动创建默认配置");
            return new DisruptorMultiConfig();
        });
    }
}
