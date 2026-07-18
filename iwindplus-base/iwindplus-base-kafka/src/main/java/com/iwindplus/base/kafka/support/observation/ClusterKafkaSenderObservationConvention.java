/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support.observation;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ObservationConstant;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;
import java.util.ArrayList;
import java.util.List;

/**
 * Kafka发送转换器
 *
 * @author zengdegui
 * @since 2026/05/08 12:13
 */
public class ClusterKafkaSenderObservationConvention implements ObservationConvention<KafkaSenderObservationContext> {

    @Override
    public boolean supportsContext(Context context) {
        return context instanceof KafkaSenderObservationContext;
    }

    @Override
    public String getName() {
        return KafkaConstant.KAFKA_PRODUCER;
    }

    @Override
    public String getContextualName(KafkaSenderObservationContext context) {
        return "Kafka producer";
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(KafkaSenderObservationContext context) {
        List<KeyValue> list = new ArrayList<>(10);
        list.add(KeyValue.of(
            KafkaConstant.CLUSTER,
            context.getCluster()
        ));

        if (CharSequenceUtil.isNotBlank(context.getTopic())) {
            list.add(KeyValue.of(
                KafkaConstant.TOPIC,
                context.getTopic()
            ));
        }

        if (CharSequenceUtil.isNotBlank(context.getKey())) {
            list.add(KeyValue.of(
                KafkaConstant.KEY,
                context.getKey()
            ));
        }

        return KeyValues.of(list);
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(KafkaSenderObservationContext context) {
        String error = context.getError() == null
            ? ObservationConstant.NONE
            : context.getError().getClass().getSimpleName();

        return KeyValues.of(
            ObservationConstant.EXCEPTION, error
        );
    }
}
