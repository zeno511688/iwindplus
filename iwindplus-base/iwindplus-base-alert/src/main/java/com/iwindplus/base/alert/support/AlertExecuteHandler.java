/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.support;

import com.iwindplus.base.alert.domain.dto.AlertAppRequestDTO;
import com.iwindplus.base.alert.domain.dto.AlertWebhookRequestDTO;
import com.iwindplus.base.alert.domain.enums.AlertChannelTypeEnum;
import com.iwindplus.base.alert.service.BaseService;

/**
 * 告警策略标识接口.
 *
 * <p>策略模式：不同的告警渠道实现此接口。每个配置对应一个运行时策略实例。</p>
 *
 * @author zengdegui
 * @since 2026/03/03 17:44
 */
public interface AlertExecuteHandler extends BaseService {

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
