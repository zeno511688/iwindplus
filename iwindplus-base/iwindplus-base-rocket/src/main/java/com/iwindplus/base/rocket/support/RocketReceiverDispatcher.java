/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.support;

/**
 * Rocket接收调度器.
 *
 * @author zengdegui
 * @since 2026/05/08 16:36
 */
public interface RocketReceiverDispatcher {

    /**
     * 执行分发消息.
     *
     * @param handler 消息处理助手
     */
    void dispatch(RocketMessageHandler handler);
}