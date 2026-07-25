/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.disruptor.support.observation;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.disruptor.domain.constant.DisruptorConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import java.util.ArrayList;
import java.util.List;

/**
 * Disruptor观察转换器
 *
 * @author zengdegui
 * @since 2026/05/08 11:57
 */
public class DisruptorObservationConvention implements ObservationConvention<DisruptorObservationContext> {

    @Override
    public boolean supportsContext(Context context) {
        return context instanceof DisruptorObservationContext;
    }

    @Override
    public String getName() {
        return DisruptorConstant.DISRUPTOR;
    }

    @Override
    public String getContextualName(DisruptorObservationContext context) {
        return context.getHandler();
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(
        DisruptorObservationContext context) {

        List<KeyValue> list = new ArrayList<>(16);

        // 事件处理器
        add(list, DisruptorConstant.HANDLER, context.getHandler());
        // 来源
        add(list, DisruptorConstant.SOURCE, context.getSource());
        // 目标
        add(list, DisruptorConstant.DESTINATION, context.getDestination());
        // 异常类型
        add(list, ObservationConstant.EXCEPTION, getException(context));

        return KeyValues.of(list);
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(
        DisruptorObservationContext context) {

        return KeyValues.empty();
    }

    /**
     * 获取异常名称.
     *
     * @param context context
     * @return exception
     */
    private String getException(
        DisruptorObservationContext context) {

        Throwable error = context.getError();

        return error == null
            ? ObservationConstant.NONE
            : error.getClass()
                .getSimpleName();
    }

    /**
     * 添加tag.
     *
     * @param list  tag集合
     * @param key   key
     * @param value value
     */
    private void add(
        List<KeyValue> list,
        String key,
        String value) {

        if (CharSequenceUtil.isNotBlank(value)) {
            list.add(
                KeyValue.of(
                    key,
                    value
                )
            );
        }
    }
}