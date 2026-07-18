/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.client.autoconfigure;

import com.iwindplus.flow.domain.constant.FlowConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 流程服务客户端配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@EnableFeignClients(basePackages = {FlowConstant.FLOW_CLIENT_SCAN_BASE_PACKAGE})
public class FlowClientAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.info("Initializing the Flow API.");
    }
}

