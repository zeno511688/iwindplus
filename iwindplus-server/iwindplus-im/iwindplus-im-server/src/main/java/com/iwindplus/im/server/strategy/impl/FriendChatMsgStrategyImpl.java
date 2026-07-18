/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.im.domain.dto.FriendChatMsgDTO;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import com.iwindplus.im.server.dal.model.FriendChatMsgDO;
import com.iwindplus.im.server.service.FriendChatMsgService;
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

/**
 * 好友聊天消息策略实现类.
 *
 * @author zengdegui
 * @since 2025/09/21 20:33
 */
@Slf4j
@Service
public class FriendChatMsgStrategyImpl extends AbstractWsMsgStrategyImpl implements WsMsgStrategy {

    @Resource
    private FriendChatMsgService friendChatMsgService;

    @Resource
    private OrgClient orgClient;

    @Override
    public CommandEnum support() {
        return CommandEnum.FRIEND_CHAT_MSG;
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

        FriendChatMsgDTO param = BeanUtil.copyProperties(msg, FriendChatMsgDTO.class);
        param.setSenderId(msg.getSendUserId());
        param.setOrgId(msg.getSendOrgId());
        param.setReceiverId(msg.getReceiverId());
        param.setSendStatus(SendStatusEnum.TO_BE_SENT);
        this.friendChatMsgService.save(param);

        final TioConfig tioConfig = this.getTioConfig(ctx);
        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByUserid(tioConfig, msg.getReceiverId().toString());
        if (Objects.nonNull(channelContextSetWithLock) && 0 < channelContextSetWithLock.size()) {
            FriendChatMsgDO entity = FriendChatMsgDO.builder()
                .id(param.getId())
                .sendTime(LocalDateTime.now())
                .build();

            msg.setMsgId(param.getId());
            final Boolean flag = this.sendToUserMsg(msg, tioConfig);
            if (Boolean.TRUE.equals(flag)) {
                entity.setSendStatus(SendStatusEnum.SUCCESS);
            } else {
                entity.setSendStatus(SendStatusEnum.FAILED);
            }
            this.friendChatMsgService.updateById(entity);
        }
    }
}
