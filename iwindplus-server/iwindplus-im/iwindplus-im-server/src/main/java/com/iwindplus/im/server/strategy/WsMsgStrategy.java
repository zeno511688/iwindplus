/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.server.strategy;

import com.iwindplus.im.domain.dto.WsSendMsgDTO;
import com.iwindplus.im.domain.enums.CommandEnum;
import org.tio.core.ChannelContext;

/**
 * websocket 消息策略.
 *
 * @author zengdegui
 * @since 2025/09/21 20:18
 */
public interface WsMsgStrategy {

    /**
     * 获取支持的指令.
     *
     * @return CommandEnum
     */
    CommandEnum support();

    /**
     * 发送消息.
     *
     * @param entity 消息实体
     * @param ctx    通道上下文
     */
    void send(WsSendMsgDTO entity, ChannelContext ctx);
}
