/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.support.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdBO;
import com.iwindplus.base.async.cmd.domain.property.AsyncCmdProperty;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdDispatchHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象异步命令调度助手策略实现类.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractAsyncCmdDispatchHandler implements AsyncCmdDispatchHandler {

    private final AsyncCmdProperty property;
    private final AsyncCmdService asyncCmdService;

    @Override
    public void execute(AsyncCmdBO entity) {
        final AbstractAsyncCmdDispatchHandler proxy = SpringUtil.getBean(this.getClass());
        proxy.doExecute(entity);
    }

    /**
     * 执行.
     *
     * @param entity 对象
     */
    protected abstract void doExecute(AsyncCmdBO entity);
}
