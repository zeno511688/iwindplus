/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.auth.server.listener;

import com.iwindplus.auth.domain.event.LoginLogEvent;
import com.iwindplus.base.domain.dto.MessageBaseDTO;
import com.iwindplus.base.domain.enums.OperateTypeEnum;
import com.iwindplus.base.kafka.core.KafkaTemplateRouter;
import com.iwindplus.base.kafka.domain.property.KafkaMultiProperty;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.log.domain.dto.LoginLogDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 登录日志监听器.
 *
 * @author zengdegui
 * @since 2025/03/21 21:51
 */
@Slf4j
@Component
public class LoginLogListener {

    @Resource
    private KafkaMultiProperty kafkaProperty;

    @Resource
    private KafkaTemplateRouter kafkaTemplateRouter;

    /**
     * 登录日志监听保存数据.
     *
     * @param event 登录日志事件
     */
    @Async
    @EventListener(LoginLogEvent.class)
    public void onApplicationEvent(LoginLogEvent event) {
        log.info("登陆日志发布事件");

        final MessageBaseDTO<LoginLogDTO> messageDTO = new MessageBaseDTO();
        messageDTO.setOperateType(OperateTypeEnum.ADD.getValue());
        messageDTO.setBizType("loginLog");
        messageDTO.setData(event.getLogData());

        final String topicName = kafkaProperty.listTopic(kafkaProperty.getDefaultCluster()).get(0);
        Message<String> message = MessageBuilder
            .withPayload(JacksonUtil.toJsonStr(messageDTO))
            .setHeader(KafkaHeaders.TOPIC, topicName)
            .build();
        this.kafkaTemplateRouter.send(message);
    }
}
