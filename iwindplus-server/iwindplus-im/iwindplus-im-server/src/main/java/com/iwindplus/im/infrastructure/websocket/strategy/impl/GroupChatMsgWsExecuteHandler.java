/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.websocket.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.im.api.dto.WsSendMsgDTO;
import com.iwindplus.im.common.enums.CommandEnum;
import com.iwindplus.im.common.enums.SendStatusEnum;
import com.iwindplus.im.infrastructure.persistence.es.GroupChatMsgDO;
import com.iwindplus.im.infrastructure.websocket.strategy.WsMsgExecuteHandler;
import com.iwindplus.im.infrastructure.persistence.es.GroupChatMsgRepository;
import com.iwindplus.mgt.client.upms.OrgClient;
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
 * 群聊消息策略实现类.
 *
 * @author zengdegui
 * @since 2025/09/21 20:33
 */
@Slf4j
@Service
public class GroupChatMsgWsExecuteHandler extends AbstractWsMsgExecuteHandler implements WsMsgExecuteHandler {

    @Resource
    private GroupChatMsgRepository groupChatMsgRepository;

    @Resource
    private OrgClient orgClient;

    @Override
    public CommandEnum support() {
        return CommandEnum.GROUP_CHAT_MSG;
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

        GroupChatMsgDO param = BeanUtil.copyProperties(msg, GroupChatMsgDO.class, "content");
        param.setContent(JacksonUtil.toJsonStr(msg.getContent()));
        param.setSenderId(msg.getSendUserId());
        param.setOrgId(msg.getSendOrgId());
        param.setSendStatus(SendStatusEnum.TO_BE_SENT);
        this.groupChatMsgRepository.save(param);

        final TioConfig tioConfig = this.getTioConfig(ctx);
        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByGroup(tioConfig, msg.getReceiverId().toString());
        if (Objects.nonNull(channelContextSetWithLock) && 0 < channelContextSetWithLock.size()) {
            GroupChatMsgDO entity = GroupChatMsgDO.builder()
                .id(param.getId())
                .sendTime(System.currentTimeMillis())
                .build();

            msg.setMsgId(param.getId());
            String text = JacksonUtil.toJsonStr(msg);
            final WsResponse wsResponse = WsResponse.fromText(text, WsPacket.CHARSET_NAME);
            try {
                Tio.sendToGroup(tioConfig, msg.getReceiverId().toString(), wsResponse);
                entity.setSendStatus(SendStatusEnum.SUCCESS);
            } catch (Exception ex) {
                log.warn(ExceptionConstant.EXCEPTION, ex);
                entity.setSendStatus(SendStatusEnum.FAILED);
            }
            this.groupChatMsgRepository.updateById(entity);
        }
    }
}
