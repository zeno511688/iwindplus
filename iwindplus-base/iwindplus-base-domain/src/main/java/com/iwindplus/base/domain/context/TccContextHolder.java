/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.vo.TccBaseVO;
import java.util.Optional;

/**
 * tcc信息上下文对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public final class TccContextHolder {

    private static final TransmittableThreadLocal<TccBaseVO> THREAD_LOCAL = new TransmittableThreadLocal<>();

    private TccContextHolder() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 获取全局事务ID.
     *
     * @return 全局事务ID
     */
    public static String getXid() {
        TccBaseVO ctx = THREAD_LOCAL.get();
        return Optional.ofNullable(ctx).map(TccBaseVO::getXid).orElse(null);
    }

    /**
     * 获取全局事务ID.
     *
     * @param xid 全局事务ID
     */
    public static void setXid(String xid) {
        THREAD_LOCAL.set(new TccBaseVO(xid));
    }

    /**
     * 强制清空本地线程，防止内存泄漏，如手动调用了set可调用此方法确保清除.
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
