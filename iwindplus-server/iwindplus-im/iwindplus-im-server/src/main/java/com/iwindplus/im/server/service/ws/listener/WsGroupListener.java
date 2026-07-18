/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.ws.listener;

import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import com.iwindplus.im.domain.enums.MsgTypeEnum;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.GroupListener;
import org.tio.utils.lock.SetWithLock;
import org.tio.websocket.common.WsPacket;
import org.tio.websocket.common.WsResponse;

/**
 * 群组状态监听器.
 *
 * @author zengdegui
 * @since 2023/12/10 00:13
 */
@Slf4j
@Component
public class WsGroupListener implements GroupListener {

    @Override
    public void onAfterBind(ChannelContext channelContext, String group) throws Exception {
        final String userid = channelContext.userid;
        final SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByGroup(channelContext.tioConfig, group);
        if (Objects.nonNull(channelContextSetWithLock) && 0 < channelContextSetWithLock.size()) {
            final int size = channelContextSetWithLock.size();
            String msg = new StringBuilder("用户：").append(userid).append("，上线了；")
                .append("现在共有【").append(size).append("】人在线").toString();
            final WsSendMsgDTO wsMsg = WsSendMsgDTO.builder()
                .command(CommandEnum.GROUP_CHAT_NOTICE_MSG)
                .msgType(MsgTypeEnum.TEXT)
                .receiverId(Long.valueOf(group))
                .title("群聊上线通知")
                .content(msg)
                .build();
            String text = JacksonUtil.toJsonStr(wsMsg);
            final WsResponse wsResponse = WsResponse.fromText(text, WsPacket.CHARSET_NAME);
            Tio.sendToGroup(channelContext.tioConfig, group, wsResponse);
        }
    }

    @Override
    public void onAfterUnbind(ChannelContext channelContext, String group) throws Exception {

    }
}
