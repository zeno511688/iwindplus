/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.client.autoconfigure;

import com.iwindplus.setup.domain.constant.SetupConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 通用设置服务客户端配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@EnableFeignClients(basePackages = {SetupConstant.SETUP_CLIENT_SCAN_BASE_PACKAGE})
public class SetupClientAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.info("Initializing the Setup API.");
    }
}

