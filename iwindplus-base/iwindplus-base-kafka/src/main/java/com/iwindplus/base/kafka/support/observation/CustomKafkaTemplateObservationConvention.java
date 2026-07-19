/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support.observation;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SystemConstant;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import io.micrometer.common.KeyValues;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.support.micrometer.KafkaRecordSenderContext;
import org.springframework.kafka.support.micrometer.KafkaTemplateObservation.DefaultKafkaTemplateObservationConvention;

/**
 * 自定义kafka发送观察.
 *
 * @author zengdegui
 * @since 2026/07/19 17:37
 */
public class CustomKafkaTemplateObservationConvention extends DefaultKafkaTemplateObservationConvention {

    @Override
    public KeyValues getLowCardinalityKeyValues(KafkaRecordSenderContext context) {
        // 保留 Spring Kafka 默认 tag
        KeyValues defaultKeyValues = super.getLowCardinalityKeyValues(context);
        final String cluster = getHeader(context, KafkaConstant.CLUSTER);
        if (CharSequenceUtil.isBlank(cluster)) {
            return defaultKeyValues;
        }

        return KeyValues.concat(
            defaultKeyValues,
            KeyValues.of(
                KafkaConstant.CLUSTER,
                cluster
            )
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(
        KafkaRecordSenderContext context) {
        final KeyValues defaultKeyValues = super.getHighCardinalityKeyValues(context);
        final String requestId = getHeader(context, SystemConstant.REQUEST_ID);
        if (CharSequenceUtil.isBlank(requestId)) {
            return defaultKeyValues;
        }

        return KeyValues.concat(
            defaultKeyValues,
            KeyValues.of(
                SystemConstant.REQUEST_ID,
                requestId
            )
        );
    }

    private String getHeader(
        KafkaRecordSenderContext context,
        String name) {

        ProducerRecord<?, ?> record = context.getRecord();
        if (record == null) {
            return null;
        }
        final Header header = record.headers()
            .lastHeader(name);
        if (header == null) {
            return null;
        }

        return new String(
            header.value(),
            StandardCharsets.UTF_8
        );
    }
}
