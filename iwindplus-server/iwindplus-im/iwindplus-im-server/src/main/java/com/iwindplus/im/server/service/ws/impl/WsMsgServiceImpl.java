/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.service.ws.impl;

import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.domain.enums.MsgTypeEnum;
import com.iwindplus.im.server.service.ws.WsMsgService;
import com.iwindplus.im.server.factory.WsMsgStrategyFactory;
import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

/**
 * websocket 业务层接口实现类.
 *
 * @author zengdegui
 * @since 2023/12/04 23:22
 */
@Service
@Slf4j
public class WsMsgServiceImpl implements WsMsgService {

    @Resource
    private WsMsgStrategyFactory wsMsgStrategyFactory;

    @Async
    @Override
    public void sendWsMsg(WsSendMsgDTO entity) {
        this.sendWsMsg(entity, null);
    }

    @Async
    @Override
    public void sendWsMsg(WsSendMsgDTO entity, ChannelContext channelContext) {
        if (Objects.isNull(entity.getCommand())) {
            entity.setCommand(CommandEnum.PERSON_NOTICE_MSG);
        }
        if (Objects.isNull(entity.getMsgType())) {
            entity.setMsgType(MsgTypeEnum.TEXT);
        }
        this.wsMsgStrategyFactory.send(entity, channelContext);
    }
}
