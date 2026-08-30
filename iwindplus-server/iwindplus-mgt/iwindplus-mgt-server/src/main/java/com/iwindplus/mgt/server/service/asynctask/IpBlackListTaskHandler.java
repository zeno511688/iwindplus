/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynctask;

import com.iwindplus.base.async.task.domain.vo.AsyncTaskExecuteResultVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskVO;
import com.iwindplus.base.async.task.support.AsyncTaskHandler;
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
public class IpBlackListTaskHandler implements AsyncTaskHandler {

    private final KafkaTemplateRouter kafkaTemplateRouter;
    private final KafkaMultiProperty kafkaProperty;

    @Override
    public AsyncTaskExecuteResultVO execute(AsyncTaskVO entity) {
        final Map<String, Object> paramMap = entity.getParam();
        final String content = paramMap.get("content").toString();

        this.sendMessage(content);
        return AsyncTaskExecuteResultVO.success();
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
