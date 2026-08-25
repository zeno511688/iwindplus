/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.fallback;

import com.iwindplus.base.domain.exception.BizException;
import feign.FeignException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;

/**
 * Feign 默认回退工厂.
 *
 * <p>只在客户端未声明 {@code fallback} 或 {@code fallbackFactory} 时使用，默认不伪造业务成功，
 * 统一抛出服务不可用异常。</p>
 *
 * @param <T> Feign 客户端类型
 * @author zengdegui
 * @since 2026/8/25
 */
@Slf4j
public class DefaultFeignFallbackFactory<T> implements FallbackFactory<T> {

    /**
     * 客户端名称.
     */
    private final String clientName;

    /**
     * 客户端接口类型.
     */
    private final Class<T> targetType;

    /**
     * 构造默认回退工厂.
     */
    @SuppressWarnings("unchecked")
    public DefaultFeignFallbackFactory() {
        final DefaultFeignFallbackContextHolder.DefaultFeignFallbackContext context =
            DefaultFeignFallbackContextHolder.getContext();
        this.clientName = context.clientName();
        this.targetType = (Class<T>) context.targetType();
    }

    @Override
    public T create(Throwable cause) {
        final InvocationHandler invocationHandler = new DefaultFallbackInvocationHandler(this.clientName, cause);
        return this.targetType.cast(Proxy.newProxyInstance(
            this.targetType.getClassLoader(), new Class<?>[]{this.targetType}, invocationHandler));
    }

    /**
     * 默认回退调用处理器.
     */
    private record DefaultFallbackInvocationHandler(String clientName, Throwable cause)
        implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "DefaultFeignFallback[" + this.clientName + "]";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                };
            }
            log.warn("Feign client fallback triggered, client={}, method={}.", this.clientName,
                method.getName(), this.cause);
            final BizException exception = new BizException(HttpStatus.SERVICE_UNAVAILABLE);
            if (this.cause != null && !(this.cause instanceof FeignException)) {
                exception.initCause(this.cause);
            }
            throw exception;
        }
    }

}
