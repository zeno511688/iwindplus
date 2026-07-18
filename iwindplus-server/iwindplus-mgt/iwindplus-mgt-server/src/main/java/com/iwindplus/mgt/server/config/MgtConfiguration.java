/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.config;

import com.iwindplus.mgt.server.config.property.MgtProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 管理服务配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({MgtProperty.class})
public class MgtConfiguration {

}

