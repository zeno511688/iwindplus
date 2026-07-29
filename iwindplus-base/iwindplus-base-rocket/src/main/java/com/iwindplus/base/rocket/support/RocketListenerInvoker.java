/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support;

import com.iwindplus.base.rocket.domain.dto.RocketMultiListenerMetaDTO;
import java.util.List;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * rocket监听器调用器.
 *
 * @author zengdegui
 * @since 2026/07/28 23:02
 */
public interface RocketListenerInvoker {

    /**
     * 预热，创建参数解析器.
     *
     * @param metas Rocket监听器元数据集合
     */
    void preWarm(List<RocketMultiListenerMetaDTO> metas);

    /**
     * 调用监听器.
     *
     * @param meta Rocket监听器元数据（必填）
     * @param msgs Rocket消息（必填）
     */
    void invoke(
        RocketMultiListenerMetaDTO meta,
        List<MessageExt> msgs);
}
