/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.server.service.ws.filter;

import lombok.Data;
import org.tio.core.ChannelContext;
import org.tio.core.ChannelContextFilter;

/**
 * 排除自己给自己发送消息过滤器.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Data
public class ExcludeMineChannelContextFilter implements ChannelContextFilter {
    private ChannelContext currentContext;

    public ExcludeMineChannelContextFilter(ChannelContext channelContext) {
        this.currentContext = channelContext;
    }

    @Override
    public boolean filter(ChannelContext channelContext) {
        return !this.currentContext.userid.equals(channelContext.userid);
    }
}
