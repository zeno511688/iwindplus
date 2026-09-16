/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.sms;

import com.iwindplus.base.domain.enums.SmsTypeEnum;
import com.iwindplus.base.sms.domain.property.SmsProperty;
import com.iwindplus.base.sms.domain.property.SmsProperty.BaseConfig;
import com.iwindplus.base.sms.factory.SmsExecuteHandlerFactory;
import com.iwindplus.base.sms.support.SmsExecuteHandler;
import com.iwindplus.base.sms.support.impl.AliyunSmsExecuteHandler;
import com.iwindplus.base.sms.support.impl.LingkaiSmsExecuteHandler;
import com.iwindplus.base.sms.support.impl.MxtongSmsExecuteHandler;
import com.iwindplus.base.sms.support.impl.QiniuSmsExecuteHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信配置.
 *
 * @author zengdegui
 * @since 2019/8/13
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "sms", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SmsProperty.class)
public class SmsConfiguration {

    /**
     * 短信策略工厂.
     *
     * @param property 短信属性配置
     * @return 策略工厂
     */
    @Bean
    public SmsExecuteHandlerFactory smsExecuteHandlerFactory(SmsProperty property) {
        List<SmsExecuteHandler> handlers = new ArrayList<>(10);
        handlers.addAll(
            this.buildHandlers(property.getAliyun(), SmsTypeEnum.ALIYUN, AliyunSmsExecuteHandler::new)
        );
        handlers.addAll(
            this.buildHandlers(property.getQiniu(), SmsTypeEnum.QINIU, QiniuSmsExecuteHandler::new)
        );
        handlers.addAll(
            this.buildHandlers(property.getLingkai(), SmsTypeEnum.LINGKAI, LingkaiSmsExecuteHandler::new)
        );
        handlers.addAll(
            this.buildHandlers(property.getMxtong(), SmsTypeEnum.MXTONG, MxtongSmsExecuteHandler::new)
        );
        SmsExecuteHandlerFactory factory = new SmsExecuteHandlerFactory(property, handlers);
        log.info("SmsExecuteHandlerFactory={}", factory);
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
    private <C extends BaseConfig> List<SmsExecuteHandler> buildHandlers(
        List<C> configs,
        SmsTypeEnum provider,
        Function<C, SmsExecuteHandler> handlerFactory) {
        List<SmsExecuteHandler> handlers = new ArrayList<>(10);
        Set<String> codes = new HashSet<>(16);
        configs.stream()
            .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
            .filter(config -> codes.add(config.getCode()))
            .forEach(config -> {
                log.info("Initializing {} SMS strategy [code={}]", provider, config.getCode());
                handlers.add(handlerFactory.apply(config));
            });
        return handlers;
    }
}
