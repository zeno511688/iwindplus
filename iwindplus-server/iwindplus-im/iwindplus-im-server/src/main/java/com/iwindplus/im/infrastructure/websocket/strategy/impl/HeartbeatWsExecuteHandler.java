/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.websocket.strategy.impl;

import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.im.api.dto.WsSendMsgDTO;
import com.iwindplus.im.common.enums.CommandEnum;
import com.iwindplus.im.infrastructure.websocket.strategy.WsMsgExecuteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.websocket.common.WsPacket;
import org.tio.websocket.common.WsResponse;

/**
 * 心跳消息策略实现类.
 *
 * @author zengdegui
 * @since 2025/09/21 20:33
 */
@Slf4j
@Service
public class HeartbeatWsExecuteHandler extends AbstractWsMsgExecuteHandler implements WsMsgExecuteHandler {

    @Override
    public CommandEnum support() {
        return CommandEnum.HEARTBEAT;
    }

    @Override
    public void send(WsSendMsgDTO msg, ChannelContext ctx) {
        String text = JacksonUtil.toJsonStr(msg);
        final WsResponse wsResponse = WsResponse.fromText(text, WsPacket.CHARSET_NAME);
        Tio.send(ctx, wsResponse);
    }
}
