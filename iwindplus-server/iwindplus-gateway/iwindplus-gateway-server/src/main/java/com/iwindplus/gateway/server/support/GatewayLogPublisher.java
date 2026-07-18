/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.server.support;

import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.kafka.core.KafkaTemplateRouter;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.log.domain.dto.GatewayLogDTO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 网关日志发布.
 *
 * @author zengdegui
 * @since 2025/08/23 18:51
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayLogPublisher {

    @Resource
    private KafkaMultiProperty kafkaProperty;

    @Resource
    private KafkaTemplateRouter kafkaTemplateRouter;

    /**
     * 发布网关日志.
     *
     * @param dto 日志数据
     */
    @Async
    public void publish(GatewayLogDTO dto) {
        log.info("网关日志发布事件");

        final MessageBaseDTO<GatewayLogDTO> messageDTO = new MessageBaseDTO();
        messageDTO.setOperateType(OperateTypeEnum.ADD.getValue());
        messageDTO.setBizType("gatewayLog");
        messageDTO.setData(dto);

        final String topicName = kafkaProperty.listTopic(kafkaProperty.getDefaultCluster()).get(0);
        Message<String> message = MessageBuilder
            .withPayload(JacksonUtil.toJsonStr(messageDTO))
            .setHeader(KafkaHeaders.TOPIC, topicName)
            .build();
        this.kafkaTemplateRouter.send(message);
    }
}
