/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support.observation;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.rabbit.domain.constant.RabbitConstant;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import java.util.ArrayList;
import java.util.List;

/**
 * Rabbit发送转换器
 *
 * @author zengdegui
 * @since 2026/05/08 11:50
 */
public class ClusterRabbitSenderObservationConvention implements ObservationConvention<RabbitSenderObservationContext> {

    @Override
    public boolean supportsContext(Context context) {
        return context instanceof RabbitSenderObservationContext;
    }

    @Override
    public String getName() {
        return RabbitConstant.RABBIT_PRODUCER;
    }

    @Override
    public String getContextualName(RabbitSenderObservationContext context) {
        return "rabbit producer";
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RabbitSenderObservationContext context) {
        List<KeyValue> list = new ArrayList<>(10);
        list.add(KeyValue.of(
            RabbitConstant.CLUSTER,
            context.getCluster()
        ));

        if (CharSequenceUtil.isNotBlank(context.getExchange())) {
            list.add(KeyValue.of(
                RabbitConstant.EXCHANGE,
                context.getExchange()
            ));
        }

        if (CharSequenceUtil.isNotBlank(context.getRoutingKey())) {
            list.add(KeyValue.of(
                RabbitConstant.ROUTING_KEY,
                context.getRoutingKey()
            ));
        }

        return KeyValues.of(list);
    }
}