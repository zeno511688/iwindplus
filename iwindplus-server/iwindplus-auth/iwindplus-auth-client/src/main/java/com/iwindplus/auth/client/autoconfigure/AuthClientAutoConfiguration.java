/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.client.autoconfigure;

import com.iwindplus.auth.domain.constant.AuthConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务客户端配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@EnableFeignClients(basePackages = {AuthConstant.AUTH_CLIENT_SCAN_BASE_PACKAGE})
public class AuthClientAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.info("Initializing the Auth API.");
    }
}

