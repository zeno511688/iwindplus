/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.websocket.domain.property.WebSocketProperty;
import com.iwindplus.base.websocket.service.WebSocketHttpServerBootstrap;
import com.iwindplus.base.websocket.service.WebSocketServerBootstrap;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tio.cluster.redisson.RedissonTioClusterTopic;

/**
 * WebSocket 配置.
 *
 * @author zengdegui
 * @since 2023/11/06 20:58
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WebSocketProperty.class)
public class WebSocketConfiguration {

    @Resource
    private RedissonClient redisson;

    @Resource
    private WebSocketProperty property;

    /**
     * 创建 RedissonTioClusterTopic.
     *
     * @return RedissonTioClusterTopic
     */
    @Bean
    public RedissonTioClusterTopic redissonTioClusterTopic() {
        final String serverName = this.getServerName();
        final RedissonTioClusterTopic redissonTioClusterTopic = new RedissonTioClusterTopic(serverName, this.redisson);
        log.info("RedissonTioClusterTopic={}", redissonTioClusterTopic);
        return redissonTioClusterTopic;
    }


    /**
     * 创建 WebSocketServerBootstrap.
     *
     * @return WebSocketServerBootstrap
     */
    @ConditionalOnProperty(prefix = "websocket.server", name = "enabled", havingValue = "true")
    @Bean
    public WebSocketServerBootstrap webSocketServerBootstrap() {
        final WebSocketServerBootstrap webSocketServerBootstrap = new WebSocketServerBootstrap(this.property);
        log.info("WebSocketServerBootstrap={}", webSocketServerBootstrap);
        return webSocketServerBootstrap;
    }

    /**
     * 创建 WebSocketHttpServerBootstrap.
     *
     * @return WebSocketHttpServerBootstrap
     */
    @ConditionalOnProperty(prefix = "websocket.server", name = "http-enabled", havingValue = "true")
    @Bean
    public WebSocketHttpServerBootstrap webSocketHttpServerBootstrap() {
        final WebSocketHttpServerBootstrap webSocketHttpServerBootstrap = new WebSocketHttpServerBootstrap(this.property);
        log.info("WebSocketHttpServerBootstrap={}", webSocketHttpServerBootstrap);
        return webSocketHttpServerBootstrap;
    }

    private String getServerName() {
        final String serverId = SpringUtil.getProperty("spring.application.name").toUpperCase();
        final String name = StrUtil.replace(serverId, SymbolConstant.HORIZONTAL_LINE, SymbolConstant.UNDERLINE);
        return String.format("%s%s", SymbolConstant.UNDERLINE, name);
    }
}
