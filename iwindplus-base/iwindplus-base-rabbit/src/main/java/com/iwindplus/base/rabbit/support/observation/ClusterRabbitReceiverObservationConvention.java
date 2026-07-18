/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support.observation;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.rabbit.domain.constant.RabbitConstant;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import java.util.ArrayList;
import java.util.List;

/**
 * Rabbit接收转换器
 *
 * @author zengdegui
 * @since 2026/05/08 11:57
 */
public class ClusterRabbitReceiverObservationConvention implements ObservationConvention<RabbitReceiverObservationContext> {

    @Override
    public boolean supportsContext(Context context) {
        return context instanceof RabbitReceiverObservationContext;
    }

    @Override
    public String getName() {
        return RabbitConstant.RABBIT_CONSUMER;
    }

    @Override
    public String getContextualName(RabbitReceiverObservationContext context) {
        return "rabbit consumer";
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RabbitReceiverObservationContext context) {
        List<KeyValue> list = new ArrayList<>(10);
        list.add(KeyValue.of(
            RabbitConstant.CLUSTER,
            context.getCluster()
        ));

        if (ArrayUtil.isNotEmpty(context.getQueues())) {
            final String val = String.join(",", context.getQueues());
            list.add(KeyValue.of(
                RabbitConstant.QUEUES,
                val
            ));
        }

        if (CharSequenceUtil.isNotBlank(context.getGroup())) {
            list.add(KeyValue.of(
                RabbitConstant.GROUP,
                context.getGroup()
            ));
        }

        return KeyValues.of(list);
    }

}
