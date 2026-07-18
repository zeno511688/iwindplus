/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.executor;

import com.iwindplus.base.alert.domain.dto.AlertAppRequestDTO;
import com.iwindplus.base.alert.domain.dto.AlertWebhookRequestDTO;
import com.iwindplus.base.alert.domain.enums.AlertChannelTypeEnum;

/**
 * 告警执行器接口.
 *
 * @author zengdegui
 * @since 2026/03/03 17:44
 */
public interface AlertExecutor {

    /**
     * 获取当前渠道类型.
     *
     * @return AlertChannelTypeEnum
     */
    AlertChannelTypeEnum getChannelType();

    /**
     * 发送应用消息.
     *
     * @param entity 对象
     */
    void sendAppMsg(AlertAppRequestDTO entity);

    /**
     * 发送webhook消息.
     *
     * @param entity 对象
     */
    void sendWebhookMsg(AlertWebhookRequestDTO entity);
}
