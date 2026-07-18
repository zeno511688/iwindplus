/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.util.Map;

/**
 * 请求头信息上下文对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public final class HeaderContextHolder {

    private static final TransmittableThreadLocal<Map<String, String>> THREAD_LOCAL = new TransmittableThreadLocal<>();

    private HeaderContextHolder() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 获取.
     *
     * @return Map<String, String>
     */
    public static Map<String, String> getContext() {
        return THREAD_LOCAL.get();
    }

    /**
     * 设置.
     *
     * @param entity 对象
     */
    public static void setContext(Map<String, String> entity) {
        THREAD_LOCAL.set(entity);
    }

    /**
     * 强制清空本地线程，防止内存泄漏，如手动调用了set可调用此方法确保清除.
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
