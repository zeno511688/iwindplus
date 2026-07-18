/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
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
     * 获取跟踪唯一标识.
     */
    public static String getTraceId() {
        return MDC.get(HeaderConstant.TRACE_ID);
    }

    /**
     * 设置跟踪唯一标识.
     *
     * @param traceId 跟踪唯一标识
     */
    public static void setTraceId(String traceId) {
        if (CharSequenceUtil.isBlank(traceId)) {
            return;
        }
        MDC.put(HeaderConstant.TRACE_ID, traceId);
    }

    /**
     * 清理MDC.
     */
    public static void clearTraceId() {
        MDC.remove(HeaderConstant.TRACE_ID);
    }

    /**
     * 写入 Reactor Context.
     *
     * @param mono mono
     * @param <T>  泛型
     * @return Mono<T>
     */
    public static <T> Mono<T> withTraceId(Mono<T> mono) {
        String traceId = MdcUtil.getTraceId();

        return Mono.deferContextual(ctxView -> {
            String ctxTraceId = ctxView.getOrDefault(HeaderConstant.TRACE_ID, traceId);

            return mono
                .contextWrite(ctx -> ctx.put(HeaderConstant.TRACE_ID, ctxTraceId))
                .doOnEach(signal -> {
                    if (!signal.isOnComplete() && !signal.isOnError()) {
                        return;
                    }
                    MdcUtil.setTraceId(ctxTraceId);
                })
                .doFinally(signal -> MdcUtil.clearTraceId());
        });
    }

    /**
     * 线程池场景下，包装Runnable
     *
     * @param runnable runnable
     * @return Runnable
     */
    public static Runnable wrap(Runnable runnable) {
        String traceId = MdcUtil.getTraceId();

        return () -> {
            try {
                if (traceId != null) {
                    MdcUtil.setTraceId(traceId);
                }
                runnable.run();
            } finally {
                MdcUtil.clearTraceId();
            }
        };
    }
}
