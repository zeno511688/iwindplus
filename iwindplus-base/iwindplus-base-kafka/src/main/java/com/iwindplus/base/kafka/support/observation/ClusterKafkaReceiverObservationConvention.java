/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support.observation;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import java.util.ArrayList;
import java.util.List;

/**
 * Kafka接收转换器
 *
 * @author zengdegui
 * @since 2026/05/08 11:57
 */
public class ClusterKafkaReceiverObservationConvention implements ObservationConvention<KafkaReceiverObservationContext> {

    @Override
    public boolean supportsContext(Context context) {
        return context instanceof KafkaReceiverObservationContext;
    }

    @Override
    public String getName() {
        return KafkaConstant.KAFKA_CONSUMER;
    }

    @Override
    public String getContextualName(KafkaReceiverObservationContext context) {
        return "kafka consumer";
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(KafkaReceiverObservationContext context) {
        List<KeyValue> list = new ArrayList<>(10);
        list.add(KeyValue.of(
            KafkaConstant.CLUSTER,
            context.getCluster()
        ));

        if (ArrayUtil.isNotEmpty(context.getTopics())) {
            final String val = String.join(",", context.getTopics());
            list.add(KeyValue.of(
                KafkaConstant.TOPIC,
                val
            ));
        }

        if (CharSequenceUtil.isNotBlank(context.getGroup())) {
            list.add(KeyValue.of(
                KafkaConstant.GROUP,
                context.getGroup()
            ));
        }

        return KeyValues.of(list);
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(KafkaReceiverObservationContext context) {
        String error = context.getError() == null
            ? ObservationConstant.NONE
            : context.getError().getClass().getSimpleName();

        return KeyValues.of(
            ObservationConstant.EXCEPTION, error
        );
    }
}
