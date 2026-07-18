/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.client.autoconfigure;

import com.iwindplus.dtx.domain.constant.DtxConstant;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 分布式服务客户端配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@EnableFeignClients(basePackages = {DtxConstant.DTX_CLIENT_SCAN_BASE_PACKAGE})
public class DtxClientAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.info("Initializing the Dtx API.");
    }
}

