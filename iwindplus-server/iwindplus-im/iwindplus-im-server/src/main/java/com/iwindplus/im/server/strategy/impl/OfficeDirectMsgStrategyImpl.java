/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.strategy.impl;

import cn.hutool.core.collection.CollUtil;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.domain.enums.SendStatusEnum;
import com.iwindplus.im.domain.vo.DirectMsgVO;
import com.iwindplus.im.server.dal.model.DirectMsgDO;
import com.iwindplus.im.server.service.DirectMsgService;
import com.iwindplus.im.server.strategy.WsMsgStrategy;
import com.iwindplus.mgt.client.power.OrgClient;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
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
public class OfficeDirectMsgStrategyImpl extends AbstractWsMsgStrategyImpl implements WsMsgStrategy {

    @Resource
    private DirectMsgService directMsgService;

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

        final List<DirectMsgVO> list = this.directMsgService.listByUnSendSuccess(msg.getSendUserId(), msg.getSendOrgId());
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
            .sendTime(LocalDateTime.now())
            .build();

        final Boolean flag = this.sendToUserMsg(msg, tioConfig);

        if (Boolean.TRUE.equals(flag)) {
            param.setSendStatus(SendStatusEnum.SUCCESS);
        } else {
            param.setSendStatus(SendStatusEnum.FAILED);
        }
        this.directMsgService.updateById(param);
    }
}
