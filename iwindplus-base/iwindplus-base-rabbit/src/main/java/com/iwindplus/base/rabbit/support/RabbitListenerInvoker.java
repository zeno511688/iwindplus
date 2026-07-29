/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.support;

import com.iwindplus.base.rabbit.domain.dto.RabbitMultiListenerMetaDTO;
import com.rabbitmq.client.Channel;
import java.util.List;
import org.springframework.amqp.core.Message;

/**
 * rabbit监听器调用器.
 *
 * @author zengdegui
 * @since 2026/07/28 23:02
 */
public interface RabbitListenerInvoker {

    /**
     * 预热，创建参数解析器.
     *
     * @param metas Rabbit监听器元数据集合
     */
    void preWarm(List<RabbitMultiListenerMetaDTO> metas);

    /**
     * 调用监听器.
     *
     * @param meta     Rabbit监听器元数据（必填）
     * @param messages Rabbit消息（必填）
     * @param channel  通道
     */
    void invoke(
        RabbitMultiListenerMetaDTO meta,
        List<Message> messages,
        Channel channel);
}
