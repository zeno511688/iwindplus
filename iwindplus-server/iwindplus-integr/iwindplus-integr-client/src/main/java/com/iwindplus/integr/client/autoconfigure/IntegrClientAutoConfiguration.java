/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.integr.client.autoconfigure;

import com.iwindplus.integr.common.constant.IntegrConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 集成服务客户端配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@EnableFeignClients(basePackages = {IntegrConstant.INTEGR_CLIENT_SCAN_BASE_PACKAGE})
public class IntegrClientAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.info("Initializing the Integr API.");
    }
}

