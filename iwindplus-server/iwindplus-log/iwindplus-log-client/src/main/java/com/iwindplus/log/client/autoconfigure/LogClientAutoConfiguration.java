/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.client.autoconfigure;

import com.iwindplus.log.domain.constant.LogConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 日志服务客户端配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@EnableFeignClients(basePackages = {LogConstant.LOG_CLIENT_SCAN_BASE_PACKAGE})
public class LogClientAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.info("Initializing the Log API.");
    }
}

