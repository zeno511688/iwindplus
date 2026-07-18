/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.strategy.impl;

import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.server.strategy.WsMsgStrategy;
import com.iwindplus.mgt.client.power.OrgClient;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.utils.lock.SetWithLock;
import org.tio.websocket.common.WsPacket;
import org.tio.websocket.common.WsResponse;

/**
 * 群聊通知消息策略实现类.
 *
 * @author zengdegui
 * @since 2025/09/21 20:33
 */
@Slf4j
@Service
public class GroupChatNoticeMsgStrategyImpl extends AbstractWsMsgStrategyImpl implements WsMsgStrategy {

    @Resource
    private OrgClient orgClient;

    @Override
    public CommandEnum support() {
        return CommandEnum.GROUP_CHAT_NOTICE_MSG;
    }

    @Override
    public void send(WsSendMsgDTO msg, ChannelContext ctx) {
        if (Objects.isNull(msg.getSendOrgId()) && Objects.nonNull(msg.getSendUserId())) {
            msg.setSendOrgId(this.orgClient.getOrgId(msg.getSendUserId()).getBizData());
        }

        final Long receiverId = msg.getReceiverId();
        if (Objects.isNull(receiverId)) {
            return;
        }

        final TioConfig tioConfig = this.getTioConfig(ctx);
        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByGroup(tioConfig, msg.getReceiverId().toString());
        if (Objects.nonNull(channelContextSetWithLock) && 0 < channelContextSetWithLock.size()) {
            String text = JacksonUtil.toJsonStr(msg);
            final WsResponse wsResponse = WsResponse.fromText(text, WsPacket.CHARSET_NAME);
            Tio.sendToGroup(tioConfig, msg.getReceiverId().toString(), wsResponse);
        }
    }
}
