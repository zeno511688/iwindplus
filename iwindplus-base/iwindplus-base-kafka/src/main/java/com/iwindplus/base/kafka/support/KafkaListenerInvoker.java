/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.kafka.support;

import com.iwindplus.base.kafka.domain.dto.KafkaMultiListenerMetaDTO;
import java.util.List;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

/**
 * kafka监听器调用器.
 *
 * @author zengdegui
 * @since 2026/07/28 23:02
 */
public interface KafkaListenerInvoker {

    /**
     * 预热，创建参数解析器.
     *
     * @param metas kafka监听器元数据集合
     */
    void preWarm(List<KafkaMultiListenerMetaDTO> metas);

    /**
     * 调用监听器.
     *
     * @param meta     kafka监听器元数据（必填）
     * @param records  kafka消息（必填）
     * @param ack      kafka ack（可选）
     * @param consumer kafka消费者（可选）
     */
    void invoke(
        KafkaMultiListenerMetaDTO meta,
        List<ConsumerRecord<String, Object>> records,
        Acknowledgment ack,
        Consumer<?, ?> consumer);
}
