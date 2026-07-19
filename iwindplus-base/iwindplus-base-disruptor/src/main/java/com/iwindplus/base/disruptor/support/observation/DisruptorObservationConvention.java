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
        add(list, DisruptorConstant.HANDLER, context.getHandler());
        add(list, DisruptorConstant.SEQUENCE, context.getSequence());
        add(list, DisruptorConstant.END_OF_BATCH, context.getEndOfBatch());
        add(list, DisruptorConstant.SOURCE, context.getSource());
        add(list, DisruptorConstant.DESTINATION, context.getDestination());

        String error = context.getError() == null
            ? ObservationConstant.NONE
            : context.getError().getClass().getSimpleName();
        add(list, ObservationConstant.EXCEPTION, error);
        return KeyValues.of(list);
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(DisruptorObservationContext context) {
        return KeyValues.empty();
    }

    private void add(
        List<KeyValue> list,
        String key,
        String value) {

        if (CharSequenceUtil.isNotBlank(value)) {
            list.add(KeyValue.of(key, value));
        }
    }
}
