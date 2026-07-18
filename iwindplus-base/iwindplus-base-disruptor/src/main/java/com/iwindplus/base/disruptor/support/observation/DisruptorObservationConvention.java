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
        return DisruptorConstant.DISRUPTOR_CONSUMER;
    }

    @Override
    public String getContextualName(DisruptorObservationContext context) {
        return context.getHandler();
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(DisruptorObservationContext context) {
        List<KeyValue> list = new ArrayList<>(10);
        list.add(KeyValue.of(
            DisruptorConstant.HANDLER,
            context.getHandler()
        ));

        if (CharSequenceUtil.isNotBlank(context.getSource())) {
            list.add(KeyValue.of(
                DisruptorConstant.SOURCE,
                context.getSource()
            ));
        }

        if (CharSequenceUtil.isNotBlank(context.getDestination())) {
            list.add(KeyValue.of(
                DisruptorConstant.DESTINATION,
                context.getDestination()
            ));
        }

        return KeyValues.of(list);
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(DisruptorObservationContext context) {
        String error = context.getError() == null
            ? ObservationConstant.NONE
            : context.getError().getClass().getSimpleName();

        return KeyValues.of(
            ObservationConstant.EXCEPTION, error,
            DisruptorConstant.SEQUENCE, context.getSequence()
        );
    }
}
