/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.client.autoconfigure;

import com.iwindplus.mgt.domain.constant.MgtConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 管理服务客户端配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@EnableFeignClients(basePackages = {MgtConstant.MGT_CLIENT_SCAN_BASE_PACKAGE})
public class MgtClientAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.info("Initializing the Mgt API.");
    }
}

