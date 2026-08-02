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
     * 分组合并数据，预热
     *
     * @param metas 元数据集合
     * @return List<RabbitMultiListenerMetaDTO>
     */
    List<RabbitMultiListenerMetaDTO> listGroupMergePreWarm(List<RabbitMultiListenerMetaDTO> metas);

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
