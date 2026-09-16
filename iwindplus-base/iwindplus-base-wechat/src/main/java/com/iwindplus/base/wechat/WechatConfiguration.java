/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat;

import com.iwindplus.base.wechat.domain.property.WechatProperty;
import com.iwindplus.base.wechat.factory.WechatMaExecuteHandlerFactory;
import com.iwindplus.base.wechat.factory.WechatMpExecuteHandlerFactory;
import com.iwindplus.base.wechat.factory.WechatPayExecuteHandlerFactory;
import com.iwindplus.base.wechat.support.impl.WechatMaExecuteHandlerImpl;
import com.iwindplus.base.wechat.support.impl.WechatMpExecuteHandlerImpl;
import com.iwindplus.base.wechat.support.impl.WechatPayExecuteHandlerImpl;
import com.iwindplus.base.wechat.support.WechatMaExecuteHandler;
import com.iwindplus.base.wechat.support.WechatMpExecuteHandler;
import com.iwindplus.base.wechat.support.WechatPayExecuteHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 微信配置管理.
 *
 * <p>ma/mp/pay 各自独立支持多配置，每个配置对应一个运行时策略实例，通过配置编码区分。</p>
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WechatProperty.class)
@ConditionalOnProperty(prefix = "wechat", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WechatConfiguration {

    /**
     * 创建微信小程序策略工厂.
     *
     * @param property            属性配置
     * @param stringRedisTemplate redis模板
     * @return WechatMaExecuteHandlerFactory
     */
    @Bean
    public WechatMaExecuteHandlerFactory wechatMaExecuteHandlerFactory(WechatProperty property,
        StringRedisTemplate stringRedisTemplate) {
        List<WechatMaExecuteHandler> handlers = this.buildHandlers(property.getMa(),
            config -> Boolean.TRUE.equals(config.getEnabled()),
            WechatProperty.MaConfig::getCode,
            config -> new WechatMaExecuteHandlerImpl(config, stringRedisTemplate)
        );
        return new WechatMaExecuteHandlerFactory(property, handlers);
    }

    /**
     * 创建微信公众号策略工厂.
     *
     * @param property            属性配置
     * @param stringRedisTemplate redis模板
     * @return WechatMpExecuteHandlerFactory
     */
    @Bean
    public WechatMpExecuteHandlerFactory wechatMpExecuteHandlerFactory(WechatProperty property,
        StringRedisTemplate stringRedisTemplate) {
        List<WechatMpExecuteHandler> handlers = this.buildHandlers(property.getMp(),
            config -> Boolean.TRUE.equals(config.getEnabled()),
            WechatProperty.MpConfig::getCode,
            config -> new WechatMpExecuteHandlerImpl(config, stringRedisTemplate)
        );
        return new WechatMpExecuteHandlerFactory(property, handlers);
    }

    /**
     * 创建微信支付策略工厂.
     *
     * @param property 属性配置
     * @return WechatPayExecuteHandlerFactory
     */
    @Bean
    public WechatPayExecuteHandlerFactory wechatPayExecuteHandlerFactory(WechatProperty property) {
        List<WechatPayExecuteHandler> handlers = this.buildHandlers(property.getPay(),
            config -> Boolean.TRUE.equals(config.getEnabled()),
            WechatProperty.PayConfig::getCode,
            WechatPayExecuteHandlerImpl::new
        );
        return new WechatPayExecuteHandlerFactory(property, handlers);
    }

    /**
     * 根据配置列表构建策略实例列表（过滤未启用配置并按编码去重）.
     *
     * @param configs          配置列表
     * @param enabledPredicate 启用判断
     * @param codeExtractor    配置编码提取器
     * @param handlerFactory   策略实例工厂
     * @param <C>              配置类型
     * @param <H>              策略类型
     * @return 策略实例列表
     */
    private <C, H> List<H> buildHandlers(List<C> configs,
        Predicate<C> enabledPredicate,
        Function<C, String> codeExtractor,
        Function<C, H> handlerFactory) {
        List<H> handlers = new ArrayList<>(10);
        Set<String> codes = new HashSet<>(16);
        configs.stream()
            .filter(enabledPredicate)
            .filter(config -> codes.add(codeExtractor.apply(config)))
            .forEach(config -> handlers.add(handlerFactory.apply(config)));
        return handlers;
    }
}
