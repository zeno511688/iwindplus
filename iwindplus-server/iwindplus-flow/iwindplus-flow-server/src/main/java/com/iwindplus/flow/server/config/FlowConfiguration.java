/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.config;

import com.iwindplus.flow.server.config.property.FlowProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 流程服务配置.
 *
 * @author zengdegui
 * @since 2020/4/21
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({FlowProperty.class})
public class FlowConfiguration {

}

