/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.websocket.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.im.api.dto.WsSendMsgDTO;
import com.iwindplus.im.common.enums.CommandEnum;
import com.iwindplus.im.common.enums.SendStatusEnum;
import com.iwindplus.im.infrastructure.websocket.strategy.WsMsgExecuteHandler;
import com.iwindplus.im.infrastructure.persistence.es.AddFriendMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.AddFriendMsgRepository;
import com.iwindplus.mgt.client.upms.OrgClient;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.utils.lock.SetWithLock;

/**
 * 添加好友聊天消息策略实现类.
 *
 * @author zengdegui
 * @since 2025/09/21 20:33
 */
@Slf4j
@Service
public class AddFriendWsMsgExecuteHandler extends AbstractWsMsgExecuteHandler implements WsMsgExecuteHandler {

    @Resource
    private AddFriendMsgRepository addFriendMsgRepository;

    @Resource
    private OrgClient orgClient;

    @Override
    public CommandEnum support() {
        return CommandEnum.ADD_FRIEND_MSG;
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

        AddFriendMsgDO param = BeanUtil.copyProperties(msg, AddFriendMsgDO.class, "content");
        param.setContent(JacksonUtil.toJsonStr(msg.getContent()));
        param.setSenderId(msg.getSendUserId());
        param.setOrgId(msg.getSendOrgId());
        param.setReceiverId(msg.getReceiverId());
        param.setSendStatus(SendStatusEnum.TO_BE_SENT);
        this.addFriendMsgRepository.save(param);

        final TioConfig tioConfig = this.getTioConfig(ctx);
        SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByUserid(tioConfig, msg.getReceiverId().toString());
        if (Objects.nonNull(channelContextSetWithLock) && 0 < channelContextSetWithLock.size()) {
            AddFriendMsgDO entity = AddFriendMsgDO.builder()
                .id(param.getId())
                .sendTime(System.currentTimeMillis())
                .build();

            msg.setMsgId(param.getId());
            final Boolean flag = this.sendToUserMsg(msg, tioConfig);
            if (Boolean.TRUE.equals(flag)) {
                entity.setSendStatus(SendStatusEnum.SUCCESS);
            } else {
                entity.setSendStatus(SendStatusEnum.FAILED);
            }
            this.addFriendMsgRepository.updateById(entity);
        }
    }
}
