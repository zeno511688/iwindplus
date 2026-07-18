/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynccmd;

import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import com.iwindplus.base.kafka.core.KafkaTemplateRouter;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * IP黑名单数据发送kafka异步执行器.
 *
 * @author zengdegui
 * @since 2025/12/29 00:42
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpBlackListTaskHandler implements AsyncCmdTaskHandler {

    private final KafkaTemplateRouter kafkaTemplateRouter;
    private final KafkaMultiProperty kafkaProperty;

    @Override
    public void execute(AsyncCmdExecutorBO entity) {
        final Map<String, Object> contentMap = entity.getContent();
        final String content = contentMap.get("content").toString();

        this.sendMessage(content);
    }

    private void sendMessage(String content) {
        final String topicName = kafkaProperty.listTopic(kafkaProperty.getDefaultCluster()).get(2);
        Message<String> message = MessageBuilder
            .withPayload(content)
            .setHeader(KafkaHeaders.TOPIC, topicName)
            .build();
        this.kafkaTemplateRouter.send(message);
        log.info("IP黑名单 topic 成功={}", content);
    }
}
