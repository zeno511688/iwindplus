/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import java.util.Map;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

/**
 * mdc 工具类.
 *
 * @author zengdegui
 * @since 2026/04/07 23:42
 */
public class MdcUtil {

    private MdcUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取MDC值.
     *
     * @param key 键
     * @return String
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    /**
     * 设置MDC值.
     *
     * @param key   键
     * @param value 值
     */
    public static void set(String key, String value) {
        if (CharSequenceUtil.isBlank(value)) {
            return;
        }
        MDC.put(key, value);
    }

    /**
     * 移除MDC值.
     *
     * @param key 键
     */
    public static void remove(String key) {
        MDC.remove(key);
    }

    /**
     * 获取MDC上下文副本.
     *
     * @return Map<String, String>
     */
    public static Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * 写入 Reactor Context.
     *
     * @param mono mono
     * @param <T>  泛型
     * @return Mono<T>
     */
    public static <T> Mono<T> withTraceId(Mono<T> mono) {
        String traceId = MdcUtil.get(HeaderConstant.X_TRACE_ID);

        return Mono.deferContextual(ctxView -> {
            String ctxTraceId = ctxView.getOrDefault(HeaderConstant.X_TRACE_ID, traceId);

            return mono
                .contextWrite(ctx -> ctx.put(HeaderConstant.X_TRACE_ID, ctxTraceId))
                .doOnEach(signal -> {
                    if (!signal.isOnComplete() && !signal.isOnError()) {
                        return;
                    }
                    MdcUtil.set(HeaderConstant.X_TRACE_ID, ctxTraceId);
                })
                .doFinally(signal -> MdcUtil.remove(HeaderConstant.X_TRACE_ID));
        });
    }

    /**
     * 线程池场景下，包装Runnable
     *
     * @param runnable runnable
     * @return Runnable
     */
    public static Runnable wrap(Runnable runnable) {
        String traceId = MdcUtil.get(HeaderConstant.X_TRACE_ID);

        return () -> {
            try {
                if (traceId != null) {
                    MdcUtil.set(HeaderConstant.X_TRACE_ID, traceId);
                }
                runnable.run();
            } finally {
                MdcUtil.remove(HeaderConstant.X_TRACE_ID);
            }
        };
    }
}
