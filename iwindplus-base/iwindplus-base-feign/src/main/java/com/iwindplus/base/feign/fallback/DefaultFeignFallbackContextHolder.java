/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.fallback;

/**
 * 默认 Feign 回退上下文持有器.
 *
 * @author zengdegui
 * @since 2026/8/25
 */
public final class DefaultFeignFallbackContextHolder {

    /**
     * 当前客户端上下文.
     */
    private static final ThreadLocal<DefaultFeignFallbackContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private DefaultFeignFallbackContextHolder() {
    }

    /**
     * 设置当前客户端上下文.
     *
     * @param clientName 客户端名称
     * @param targetType 客户端接口类型
     */
    public static void setContext(String clientName, Class<?> targetType) {
        CONTEXT_HOLDER.set(new DefaultFeignFallbackContext(clientName, targetType));
    }

    /**
     * 获取当前客户端上下文.
     *
     * @return 当前客户端上下文
     */
    public static DefaultFeignFallbackContext getContext() {
        final DefaultFeignFallbackContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            throw new IllegalStateException("Default Feign fallback context is unavailable");
        }
        return context;
    }

    /**
     * 清理当前客户端上下文.
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 默认 Feign 回退上下文.
     *
     * @param clientName 客户端名称
     * @param targetType 客户端接口类型
     */
    public record DefaultFeignFallbackContext(String clientName, Class<?> targetType) {
    }

}
