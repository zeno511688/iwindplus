/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import com.iwindplus.base.kafka.core.KafkaTemplateRouter;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant;
import com.iwindplus.base.kafka.domain.constant.KafkaConstant.RetryHeadersConstant;
import com.iwindplus.base.kafka.domain.dto.KafkaErrorMessageDTO;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaBindingConfig;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaConsumerConfig;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty.KafkaConsumerLocalRetryConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;

/**
 * kafka消费错误.
 *
 * @author zengdegui
 * @since 2026/07/20 12:14
 */
@Slf4j
public record KafkaErrorHandler(
    String clusterName,
    KafkaConsumerConfig consumer,
    KafkaTemplateRouter kafkaTemplateRouter) implements ConsumerRecordRecoverer {

    @Override
    public void accept(ConsumerRecord<?, ?> record, Exception ex) {
        log.error(
            "Kafka consume failed, Preparing to send DLQ topic. cluster={}, topic={}, partition={}, offset={}",
            clusterName,
            record.topic(),
            record.partition(),
            record.offset(),
            ex
        );

        if (!enabledDlqFlag(record)) {
            return;
        }

        final KafkaConsumerLocalRetryConfig cfg = consumer.getLocalRetry();

        long now = System.currentTimeMillis();

        KafkaErrorMessageDTO message =
            KafkaErrorMessageDTO.builder()
                .cluster(clusterName)
                .originalTopic(record.topic())
                .originalPartition(record.partition())
                .originalOffset(record.offset())
                .key(String.valueOf(record.key()))
                .retryCount(cfg.getAttempts().intValue())
                .maxRetry(cfg.getAttempts().intValue())
                .exceptionType(ex.getClass().getName())
                .exceptionMessage(ex.getMessage())
                .firstFailedTime(now)
                .lastFailedTime(now)
                .build();

        Map<String, Object> headers = Map.of(
            KafkaConstant.CLUSTER, message.getCluster(),
            RetryHeadersConstant.ORIGINAL_TOPIC, message.getOriginalTopic(),
            RetryHeadersConstant.ORIGINAL_PARTITION, message.getOriginalPartition(),
            RetryHeadersConstant.ORIGINAL_OFFSET, message.getOriginalOffset(),
            RetryHeadersConstant.RETRY_COUNT, message.getRetryCount(),
            RetryHeadersConstant.MAX_RETRY, message.getMaxRetry(),
            RetryHeadersConstant.ERROR_TYPE, message.getExceptionType(),
            RetryHeadersConstant.ERROR_MESSAGE, message.getExceptionMessage(),
            RetryHeadersConstant.FIRST_FAILED_TIME, message.getFirstFailedTime(),
            RetryHeadersConstant.LAST_FAILED_TIME, message.getLastFailedTime()
        );

        try {
            kafkaTemplateRouter.send(clusterName,
                message.getOriginalTopic() + KafkaConstant.KAFKA_DLQ_SUFFIX,
                message.getKey(),
                headers, record.value().toString());
        } catch (Exception e) {
            log.error("send dlq failed", e);

            throw e;
        }
    }

    private boolean enabledDlqFlag(ConsumerRecord<?, ?> record) {
        final String topic = record.topic();
        KafkaBindingConfig kafkaBinding = consumer.getBindings().stream()
            .filter(x -> x.getTopic().equals(topic))
            .findFirst()
            .orElse(null);
        if (kafkaBinding != null && Boolean.TRUE.equals(kafkaBinding.getEnabledDlq())) {
            return true;
        }
        return false;
    }
}
