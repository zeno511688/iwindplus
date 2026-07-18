/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.service.ws;

import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import org.tio.core.ChannelContext;

/**
 * websocket 业务层接口类.
 *
 * @author zengdegui
 * @since 2023/12/04 23:22
 */
public interface WsMsgService {

    /**
     * 消息发送.
     *
     * @param entity 对象
     */
    void sendWsMsg(WsSendMsgDTO entity);

    /**
     * 消息发送.
     *
     * @param entity         对象
     * @param channelContext 通道
     */
    void sendWsMsg(WsSendMsgDTO entity, ChannelContext channelContext);
}
