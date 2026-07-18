/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.strategy.impl;

import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.websocket.service.WebSocketServerBootstrap;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.server.strategy.WsMsgStrategy;
import jakarta.annotation.Resource;
import java.util.Objects;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.utils.lock.SetWithLock;
import org.tio.websocket.common.WsPacket;
import org.tio.websocket.common.WsResponse;

/**
 * 抽象消息策略实现类.
 *
 * @author zengdegui
 * @since 2025/09/21 20:39
 */
public abstract class AbstractWsMsgStrategyImpl implements WsMsgStrategy {

    @Resource
    private WebSocketServerBootstrap webSocketServerBootstrap;

    TioConfig getTioConfig(ChannelContext channelContext) {
        TioConfig tioConfig;
        if (Objects.nonNull(channelContext)) {
            tioConfig = channelContext.getTioConfig();
        } else {
            tioConfig = this.webSocketServerBootstrap.getServerTioConfig();
        }
        return tioConfig;
    }

    Boolean sendToUserMsg(WsSendMsgDTO msg, ChannelContext channelContext) {
        final Long receiverId = msg.getReceiverId();
        if (Objects.isNull(receiverId)) {
            return false;
        }

        final TioConfig tioConfig = this.getTioConfig(channelContext);

        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByUserid(tioConfig, msg.getReceiverId().toString());
        if (Objects.nonNull(channelContextSetWithLock) && 0 < channelContextSetWithLock.size()) {
            return this.sendToUserMsg(msg, tioConfig);
        }
        return false;
    }

    Boolean sendToUserMsg(WsSendMsgDTO msg, TioConfig tioConfig) {
        String text = JacksonUtil.toJsonStr(msg);
        final WsResponse wsResponse = WsResponse.fromText(text, WsPacket.CHARSET_NAME);
        final Long receiverId = msg.getReceiverId();
        if (receiverId == null) {
            return false;
        }
        return Tio.sendToUser(tioConfig, receiverId.toString(), wsResponse);
    }

}
