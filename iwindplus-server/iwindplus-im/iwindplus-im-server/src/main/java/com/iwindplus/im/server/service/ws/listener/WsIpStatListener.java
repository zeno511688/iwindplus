/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.ws.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.intf.Packet;
import org.tio.core.stat.IpStat;
import org.tio.core.stat.IpStatListener;
import org.tio.utils.json.Json;

/**
 * ip状态监听器.
 *
 * @author zengdegui
 * @since 2023/12/09 23:58
 */
@Slf4j
@Component
public class WsIpStatListener implements IpStatListener {
    @Override
    public void onExpired(TioConfig tioConfig, IpStat ipStat) {
        if (log.isInfoEnabled()) {
            log.info("可以把统计数据入库\r\n{}", Json.toFormatedJson(ipStat));
        }
    }

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect, IpStat ipStat) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("onAfterConnected\r\n{}", Json.toFormatedJson(ipStat));
        }
    }

    @Override
    public void onDecodeError(ChannelContext channelContext, IpStat ipStat) {
        if (log.isInfoEnabled()) {
            log.info("onDecodeError\r\n{}", Json.toFormatedJson(ipStat));
        }
    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess, IpStat ipStat) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("onAfterSent\r\n{}\r\n{}", packet.logstr(), Json.toFormatedJson(ipStat));
        }
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize, IpStat ipStat) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("onAfterDecoded\r\n{}\r\n{}", packet.logstr(), Json.toFormatedJson(ipStat));
        }
    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes, IpStat ipStat) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("onAfterReceivedBytes\r\n{}", Json.toFormatedJson(ipStat));
        }
    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, IpStat ipStat, long cost) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("onAfterHandled\r\n{}\r\n{}", packet.logstr(), Json.toFormatedJson(ipStat));
        }
    }
}
