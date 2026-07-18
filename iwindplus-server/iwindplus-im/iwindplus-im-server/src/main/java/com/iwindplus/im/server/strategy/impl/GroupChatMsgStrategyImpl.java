/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.im.domain.dto.GroupChatMsgDTO;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import com.iwindplus.im.server.dal.model.GroupChatMsgDO;
import com.iwindplus.im.server.service.GroupChatMsgService;
import com.iwindplus.im.server.strategy.WsMsgStrategy;
import com.iwindplus.mgt.client.power.OrgClient;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
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
public class GroupChatMsgStrategyImpl extends AbstractWsMsgStrategyImpl implements WsMsgStrategy {

    @Resource
    private GroupChatMsgService groupChatMsgService;

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

        GroupChatMsgDTO param = BeanUtil.copyProperties(msg, GroupChatMsgDTO.class);
        param.setSenderId(msg.getSendUserId());
        param.setOrgId(msg.getSendOrgId());
        param.setSendStatus(SendStatusEnum.TO_BE_SENT);
        this.groupChatMsgService.save(param);

        final TioConfig tioConfig = this.getTioConfig(ctx);
        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByGroup(tioConfig, msg.getReceiverId().toString());
        if (Objects.nonNull(channelContextSetWithLock) && 0 < channelContextSetWithLock.size()) {
            GroupChatMsgDO entity = GroupChatMsgDO.builder()
                .id(param.getId())
                .sendTime(LocalDateTime.now())
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
            this.groupChatMsgService.updateById(entity);
        }
    }
}
