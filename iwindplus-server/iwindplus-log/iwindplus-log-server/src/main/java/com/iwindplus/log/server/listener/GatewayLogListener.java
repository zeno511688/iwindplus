/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.disruptor.core.DisruptorManager;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.kafka.domain.annotation.KafkaMultiListener;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.log.domain.dto.GatewayLogDTO;
import com.iwindplus.log.server.handler.event.GatewayLogDisruptorEventHandler;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 网关日志监听器.
 *
 * @author zengdegui
 * @since 2025/08/24 13:04
 */
@Slf4j
@Component
public class GatewayLogListener {

    @Resource
    private DisruptorManager<List<GatewayLogDTO>> disruptorManager;

    @KafkaMultiListener(
        cluster = "${kafka.multi.default-cluster}",
        topics = {"${kafka.multi.clusters.default.bindings[0].topic}"},
        group = "${kafka.multi.clusters.default.bindings[0].group}"
    )
    public void listenBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        log.info("网关日志批量消费开始, size={}", records.size());
        if (records == null || records.isEmpty()) {
            return;
        }

        List<GatewayLogDTO> batchList = new ArrayList<>(records.size());

        try {
            buildGatewayLog(records, batchList);

            if (!batchList.isEmpty()) {
                final String name = StrUtil.lowerFirst(GatewayLogDisruptorEventHandler.class.getSimpleName());
                disruptorManager.getTemplate(name).publish("kafka", "mysql", batchList);
            }

            if (ack != null) {
                ack.acknowledge();
            }

        } catch (Exception ex) {
            log.error("网关日志批量消费失败, size={}", records.size(), ex);
            throw ex;
        }
    }

    private void buildGatewayLog(List<ConsumerRecord<String, String>> records, List<GatewayLogDTO> batchList) {
        for (ConsumerRecord<String, String> record : records) {
            if (record == null || CharSequenceUtil.isBlank(record.value())) {
                continue;
            }

            MessageBaseDTO<GatewayLogDTO> messageDTO =
                JacksonUtil.parseObject(record.value(), new TypeReference<>() {
                });

            if (messageDTO == null || messageDTO.getData() == null) {
                log.warn("消息解析为空: {}", record.value());
                continue;
            }

            batchList.add(messageDTO.getData());
        }
    }
}
