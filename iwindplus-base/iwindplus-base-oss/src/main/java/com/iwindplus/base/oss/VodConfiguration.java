/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss;

import com.iwindplus.base.oss.domain.property.VodProperty;
import com.iwindplus.base.oss.factory.VodExecuteHandlerFactory;
import com.iwindplus.base.oss.support.VodExecuteHandler;
import com.iwindplus.base.oss.support.impl.AliyunVodExecuteHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 视频点播操作配置.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "vod", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(VodProperty.class)
public class VodConfiguration {

    /**
     * 创建阿里云 Vod 策略实例列表.
     *
     * @param multipartProperties 文件上传配置
     * @param property            Vod属性配置
     * @return 阿里云Vod策略实例列表
     */
    @Bean
    public List<VodExecuteHandler> aliyunVodExecuteHandlers(
        MultipartProperties multipartProperties,
        VodProperty property) {
        List<VodExecuteHandler> handlers = new ArrayList<>(10);
        Set<String> codes = new HashSet<>(16);
        property.getAliyun().stream()
            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
            .filter(config -> codes.add(config.getCode()))
            .forEach(config -> {
                log.info("Initializing Aliyun Vod strategy [code={}]", config.getCode());
                handlers.add(new AliyunVodExecuteHandler(multipartProperties, config));
            });
        return handlers;
    }

    /**
     * 创建 Vod 策略工厂.
     *
     * @param property         属性配置
     * @param executorProvider 执行器提供者
     * @return Vod策略工厂
     */
    @Bean
    public VodExecuteHandlerFactory vodExecuteHandlerFactory(
        VodProperty property,
        ObjectProvider<VodExecuteHandler> executorProvider) {
        return new VodExecuteHandlerFactory(property, executorProvider);
    }
}
