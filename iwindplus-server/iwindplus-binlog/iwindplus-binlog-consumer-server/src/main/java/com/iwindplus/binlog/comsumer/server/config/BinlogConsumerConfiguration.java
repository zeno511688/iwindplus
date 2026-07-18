/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.config;

import com.iwindplus.binlog.comsumer.server.domain.property.BinLogConsumerProperty;
import com.iwindplus.binlog.comsumer.server.listener.BinLogConsumerListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * binlog 消费者配置.
 *
 * @author zengdegui
 * @since 2025/11/21 21:45
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "binlog.consumer", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BinLogConsumerProperty.class)
public class BinlogConsumerConfiguration {

    /**
     * binlog 消费监听.
     *
     * @return BinLogConsumerListener
     */
    @Bean
    public BinLogConsumerListener binLogConsumerListener() {
        final BinLogConsumerListener binLogConsumerListener = new BinLogConsumerListener();
        log.info("BinLogConsumerListener={}", binLogConsumerListener);
        return binLogConsumerListener;
    }
}
