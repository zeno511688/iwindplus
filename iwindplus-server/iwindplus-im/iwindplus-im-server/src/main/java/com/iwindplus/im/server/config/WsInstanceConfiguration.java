/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.MetadataConstant;
import com.iwindplus.base.websocket.domain.property.WebSocketProperty;
import com.iwindplus.im.server.config.property.ImProperty;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * websocket注入服务至nacos.
 *
 * @author zengdegui
 * @since 2020/4/15
 */
@Configuration
@EnableConfigurationProperties(ImProperty.class)
@Slf4j
public class WsInstanceConfiguration {

    @Resource
    private NacosServiceManager nacosServiceManager;

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Resource
    private WebSocketProperty webSocketProperty;

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        this.registerService();
    }

    private void registerService() {
        try {
            NamingService namingService = this.nacosServiceManager.getNamingService();
            String serviceName = this.webSocketProperty.getServer().getName();
            String groupName = this.nacosDiscoveryProperties.getGroup();
            String clusterName = this.nacosDiscoveryProperties.getClusterName();
            Instance instance = new Instance();
            instance.setIp(this.nacosDiscoveryProperties.getIp());
            instance.setPort(this.webSocketProperty.getServer().getPort());
            instance.setWeight(this.nacosDiscoveryProperties.getWeight());
            instance.setClusterName(clusterName);
            instance.setServiceName(serviceName);
            final Map<String, String> metadata = this.nacosDiscoveryProperties.getMetadata();
            metadata.put(MetadataConstant.NAME, serviceName);
            instance.setMetadata(metadata);
            namingService.registerInstance(serviceName, groupName, instance);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
    }
}
