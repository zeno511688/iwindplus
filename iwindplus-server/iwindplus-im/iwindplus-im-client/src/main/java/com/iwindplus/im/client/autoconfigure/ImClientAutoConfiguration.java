/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.client.autoconfigure;

import com.iwindplus.im.domain.constant.ImConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 即时通讯服务客户端配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@EnableFeignClients(basePackages = {ImConstant.IM_CLIENT_SCAN_BASE_PACKAGE})
public class ImClientAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.info("Initializing the Im API.");
    }
}

