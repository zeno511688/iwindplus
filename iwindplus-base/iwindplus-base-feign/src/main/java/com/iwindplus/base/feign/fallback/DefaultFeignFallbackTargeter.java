/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.fallback;

import feign.Feign;
import feign.Target;
import org.springframework.cloud.openfeign.FeignCircuitBreaker;
import org.springframework.cloud.openfeign.FeignClientFactory;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.cloud.openfeign.Targeter;

/**
 * 为未声明 fallback 的 Feign 客户端补充默认回退的 Targeter 包装器.
 *
 * @author zengdegui
 * @since 2026/8/25
 */
public record DefaultFeignFallbackTargeter(Targeter delegate, boolean enabled) implements Targeter {

    @Override
    public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignClientFactory context,
        Target.HardCodedTarget<T> target) {
        if (!this.enabled || !(feign instanceof FeignCircuitBreaker.Builder) || hasExplicitFallback(factory)) {
            return this.delegate.target(factory, feign, context, target);
        }
        final Class<?> originalFallbackFactory = factory.getFallbackFactory();
        final String clientName = factory.getContextId() == null || factory.getContextId().isBlank()
            ? factory.getName() : factory.getContextId();
        try {
            factory.setFallbackFactory(DefaultFeignFallbackFactory.class);
            DefaultFeignFallbackContextHolder.setContext(clientName, target.type());
            return this.delegate.target(factory, feign, context, target);
        } finally {
            DefaultFeignFallbackContextHolder.clearContext();
            factory.setFallbackFactory(originalFallbackFactory);
        }
    }

    private boolean hasExplicitFallback(FeignClientFactoryBean factory) {
        return factory.getFallback() != void.class || factory.getFallbackFactory() != void.class;
    }

}
