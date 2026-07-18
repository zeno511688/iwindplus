/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.ws.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.websocket.server.WsServerAioListener;

/**
 * 服务状态监听器.
 *
 * @author zengdegui
 * @since 2023/12/10 00:07
 */
@Slf4j
@Component
public class WsAioListener extends WsServerAioListener {
    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        log.info("onAfterConnected={}", channelContext.userid);
        super.onAfterConnected(channelContext, isConnected, isReconnect);
    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {
        log.info("onAfterSent={}", channelContext.userid);
        super.onAfterSent(channelContext, packet, isSentSuccess);
    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {
        log.info("onBeforeClose={}", channelContext.userid);
        super.onBeforeClose(channelContext, throwable, remark, isRemove);
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {
        log.info("onAfterDecoded={}", channelContext.userid);
        super.onAfterDecoded(channelContext, packet, packetSize);
    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {
        log.info("onAfterReceivedBytes={}", channelContext.userid);
        super.onAfterReceivedBytes(channelContext, receivedBytes);
    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {
        log.info("onAfterHandled={}", channelContext.userid);
        super.onAfterHandled(channelContext, packet, cost);
    }
}
