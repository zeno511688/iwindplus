/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.support;

import cn.hutool.core.util.StrUtil;

/**
 * Disruptor 事件处理器助手.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2025/09/21 20:18
 */
public interface DisruptorEventHandler<T> {

    /**
     * 获取名称（有默认值不需要实现）.
     *
     * @return 类型
     */
    default String getName() {
        return StrUtil.lowerFirst(this.getClass().getSimpleName());
    }

    /**
     * 业务处理方法.
     *
     * @param data       数据
     * @param sequence   序列号
     * @param endOfBatch 是否批处理结束
     */
    void execute(T data, long sequence, boolean endOfBatch);
}
