/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.websocket.strategy.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.im.api.dto.WsSendMsgDTO;
import com.iwindplus.im.application.query.vo.AddFriendMsgVO;
import com.iwindplus.im.common.enums.CommandEnum;
import com.iwindplus.im.common.enums.MsgTypeEnum;
import com.iwindplus.im.common.enums.SendStatusEnum;
import com.iwindplus.im.infrastructure.websocket.strategy.WsMsgExecuteHandler;
import com.iwindplus.im.infrastructure.persistence.es.AddFriendMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.AddFriendMsgRepository;
import com.iwindplus.mgt.client.upms.OrgClient;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;

/**
 * 离线添加好友聊天消息策略实现类.
 *
 * @author zengdegui
 * @since 2025/09/21 20:33
 */
@Slf4j
@Service
public class OfficeAddFriendMsgWsExecuteHandler extends AbstractWsMsgExecuteHandler implements WsMsgExecuteHandler {

    @Resource
    private AddFriendMsgRepository addFriendMsgRepository;

    @Resource
    private OrgClient orgClient;

    @Override
    public CommandEnum support() {
        return CommandEnum.OFFLINE_ADD_FRIEND_MSG;
    }

    @Override
    public void send(WsSendMsgDTO msg, ChannelContext ctx) {
        if (Objects.isNull(msg.getSendOrgId()) && Objects.nonNull(msg.getSendUserId())) {
            msg.setSendOrgId(this.orgClient.getOrgId(msg.getSendUserId()).getBizData());
        }

        final TioConfig tioConfig = this.getTioConfig(ctx);

        final List<AddFriendMsgVO> list = this.addFriendMsgRepository.listByUnSendSuccess(msg.getSendUserId(), msg.getSendOrgId());
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(data -> this.offlineAddFriendMsg(tioConfig, msg, data));
        }
    }

    private void offlineAddFriendMsg(TioConfig tioConfig, WsSendMsgDTO wsMsg, AddFriendMsgVO entity) {
        final WsSendMsgDTO msg = WsSendMsgDTO.builder()
            .command(wsMsg.getCommand())
            .msgType(MsgTypeEnum.TEXT)
            .content(entity.getContent())
            .sendUserId(wsMsg.getSendUserId())
            .sendOrgId(wsMsg.getSendOrgId())
            .receiverId(wsMsg.getReceiverId())
            .msgId(entity.getId())
            .build();

        AddFriendMsgDO param = AddFriendMsgDO.builder()
            .id(entity.getId())
            .sendTime(System.currentTimeMillis())
            .build();

        final Boolean flag = this.sendToUserMsg(msg, tioConfig);

        if (Boolean.TRUE.equals(flag)) {
            param.setSendStatus(SendStatusEnum.SUCCESS);
        } else {
            param.setSendStatus(SendStatusEnum.FAILED);
        }
        this.addFriendMsgRepository.updateById(param);
    }

}
