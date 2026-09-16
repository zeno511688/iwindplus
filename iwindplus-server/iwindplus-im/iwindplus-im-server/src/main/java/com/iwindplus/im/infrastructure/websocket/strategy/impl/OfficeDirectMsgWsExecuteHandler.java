/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.infrastructure.websocket.strategy.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.im.api.dto.WsSendMsgDTO;
import com.iwindplus.im.application.query.vo.DirectMsgVO;
import com.iwindplus.im.common.enums.CommandEnum;
import com.iwindplus.im.common.enums.SendStatusEnum;
import com.iwindplus.im.infrastructure.websocket.strategy.WsMsgExecuteHandler;
import com.iwindplus.im.infrastructure.persistence.es.DirectMsgDO;
import com.iwindplus.im.infrastructure.persistence.es.DirectMsgRepository;
import com.iwindplus.mgt.client.upms.OrgClient;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;

/**
 * 离线直发消息策略实现类.
 *
 * @author zengdegui
 * @since 2025/09/21 20:33
 */
@Slf4j
@Service
public class OfficeDirectMsgWsExecuteHandler extends AbstractWsMsgExecuteHandler implements WsMsgExecuteHandler {

    @Resource
    private DirectMsgRepository directMsgRepository;

    @Resource
    private OrgClient orgClient;

    @Override
    public CommandEnum support() {
        return CommandEnum.OFFLINE_DIRECT_MSG;
    }

    @Override
    public void send(WsSendMsgDTO msg, ChannelContext ctx) {
        if (Objects.isNull(msg.getSendOrgId()) && Objects.nonNull(msg.getSendUserId())) {
            msg.setSendOrgId(this.orgClient.getOrgId(msg.getSendUserId()).getBizData());
        }

        final TioConfig tioConfig = this.getTioConfig(ctx);

        final List<DirectMsgVO> list = this.directMsgRepository.listByUnSendSuccess(msg.getSendUserId(), msg.getSendOrgId());
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(data -> this.offlineDirectMsg(tioConfig, msg, data));
        }
    }

    private void offlineDirectMsg(TioConfig tioConfig, WsSendMsgDTO wsMsg, DirectMsgVO entity) {
        final WsSendMsgDTO msg = WsSendMsgDTO.builder()
            .command(wsMsg.getCommand())
            .msgType(entity.getMsgType())
            .title(entity.getTitle())
            .content(entity.getContent())
            .sendUserId(wsMsg.getSendUserId())
            .receiverId(wsMsg.getReceiverId())
            .msgId(entity.getId())
            .build();

        DirectMsgDO param = DirectMsgDO.builder()
            .id(entity.getId())
            .sendTime(System.currentTimeMillis())
            .build();

        final Boolean flag = this.sendToUserMsg(msg, tioConfig);

        if (Boolean.TRUE.equals(flag)) {
            param.setSendStatus(SendStatusEnum.SUCCESS);
        } else {
            param.setSendStatus(SendStatusEnum.FAILED);
        }
        this.directMsgRepository.updateById(param);
    }
}
