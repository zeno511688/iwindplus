/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.binlog;

import com.iwindplus.base.binlog.domain.property.BinlogProperty;
import com.iwindplus.base.binlog.manager.BinlogEngineManager;
import com.iwindplus.base.binlog.handler.BinlogProcessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * binlog 配置.
 *
 * @author zengdegui
 * @since 2025/11/21 21:45
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "binlog", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BinlogProperty.class)
public class BinlogConfiguration {

    /**
     * binlog 引擎管理器.
     *
     * @return BinlogEngineManager
     */
    @Bean
    public BinlogEngineManager binlogEngineManager() {
        final BinlogEngineManager binlogEngineManager = new BinlogEngineManager();
        log.info("BinlogEngineManager={}", binlogEngineManager);
        return binlogEngineManager;
    }

    /**
     * binlog 处理助手.
     *
     * @return BinlogProcessHandler
     */
    @Bean
    public BinlogProcessHandler binlogProcessHandler() {
        final BinlogProcessHandler binlogProcessHandler = new BinlogProcessHandler();
        log.info("binlogProcessHandler={}", binlogProcessHandler);
        return binlogProcessHandler;
    }
}
