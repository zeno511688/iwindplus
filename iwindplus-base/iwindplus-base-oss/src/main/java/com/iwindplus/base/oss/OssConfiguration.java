/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.oss;

import com.iwindplus.base.domain.enums.OssTypeEnum;
import com.iwindplus.base.oss.domain.property.OssProperty;
import com.iwindplus.base.oss.domain.property.OssProperty.BaseConfig;
import com.iwindplus.base.oss.factory.OssExecuteHandlerFactory;
import com.iwindplus.base.oss.support.OssExecuteHandler;
import com.iwindplus.base.oss.support.impl.AliyunOssExecuteHandler;
import com.iwindplus.base.oss.support.impl.MinioOssExecuteHandler;
import com.iwindplus.base.oss.support.impl.QiniuOssExecuteHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对象存储配置.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "oss", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OssProperty.class)
public class OssConfiguration {

    /**
     * 创建 OSS 策略工厂.
     *
     * @param multipartProperties  文件上传配置
     * @param property             属性配置
     * @param okHttpClientProvider HTTP客户端提供器
     * @return OSS策略工厂
     */
    @Bean
    public OssExecuteHandlerFactory ossExecuteHandlerFactory(
        MultipartProperties multipartProperties,
        OssProperty property,
        ObjectProvider<OkHttpClient> okHttpClientProvider) {
        List<OssExecuteHandler> handlers = new ArrayList<>(10);
        handlers.addAll(
            this.buildHandlers(property.getAliyun(), OssTypeEnum.ALIYUN,
                config -> new AliyunOssExecuteHandler(multipartProperties, config))
        );
        handlers.addAll(
            this.buildHandlers(property.getQiniu(), OssTypeEnum.QINIU,
                config -> new QiniuOssExecuteHandler(multipartProperties, config))
        );
        handlers.addAll(
            this.buildHandlers(property.getMinio(), OssTypeEnum.MINIO,
                config -> new MinioOssExecuteHandler(multipartProperties, config, okHttpClientProvider))
        );
        final OssExecuteHandlerFactory factory = new OssExecuteHandlerFactory(property, handlers);
        log.info("OssExecuteHandlerFactory={}", factory);
        return factory;
    }

    /**
     * 根据配置列表构建策略实例列表（过滤未启用配置并按编码去重）.
     *
     * @param configs        配置列表
     * @param provider       服务商
     * @param handlerFactory 策略实例工厂
     * @param <C>            配置类型
     * @return 策略实例列表
     */
    private <C extends BaseConfig> List<OssExecuteHandler> buildHandlers(
        List<C> configs,
        OssTypeEnum provider,
        Function<C, OssExecuteHandler> handlerFactory) {
        List<OssExecuteHandler> handlers = new ArrayList<>(10);
        Set<String> codes = new HashSet<>(16);
        configs.stream()
            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
            .filter(config -> codes.add(config.getCode()))
            .forEach(config -> {
                log.info("Initializing {} OSS strategy [code={}]", provider, config.getCode());
                handlers.add(handlerFactory.apply(config));
            });
        return handlers;
    }
}
