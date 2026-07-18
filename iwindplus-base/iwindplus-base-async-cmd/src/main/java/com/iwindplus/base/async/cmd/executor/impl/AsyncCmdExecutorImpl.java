/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.executor.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdBO;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBO;
import com.iwindplus.base.async.cmd.domain.bo.AsyncCmdExecutorBaseBO;
import com.iwindplus.base.async.cmd.domain.dto.AsyncCmdSaveDTO;
import com.iwindplus.base.async.cmd.executor.AsyncCmdExecutor;
import com.iwindplus.base.async.cmd.factory.AsyncCmdDispatchHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.factory.AsyncCmdTaskHandlerStrategyFactory;
import com.iwindplus.base.async.cmd.service.AsyncCmdService;
import com.iwindplus.base.async.cmd.support.AsyncCmdDispatchHandler;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令执行器接口实现类.
 *
 * @author zengdegui
 * @since 2025/12/28 01:16
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncCmdExecutorImpl implements AsyncCmdExecutor {

    private final AsyncCmdService asyncCmdService;
    private final AsyncCmdDispatchHandlerStrategyFactory asyncCmdDispatchHandlerStrategyFactory;
    private final AsyncCmdTaskHandlerStrategyFactory asyncCmdTaskHandlerStrategyFactory;

    @Override
    public void submit(AsyncCmdExecutorBO entity) {
        // 校验参数
        this.checkSubmitParam(entity);
        // 校验执行器是否配置
        final AsyncCmdTaskHandler taskHandler = asyncCmdTaskHandlerStrategyFactory.getTaskHandler(entity.getExecutorClass().getSimpleName());
        // 保存数据
        final AsyncCmdSaveDTO param = BeanUtil.copyProperties(entity, AsyncCmdSaveDTO.class);
        param.setExecuteName(taskHandler.getExecuteName());
        this.asyncCmdService.save(param);

        // 获取调度管理器
        final AsyncCmdDispatchHandler dispatchHandler = asyncCmdDispatchHandlerStrategyFactory.getDispatchHandler(param.getDispatchMode());
        AsyncCmdBO copyData = BeanUtil.copyProperties(param, AsyncCmdBO.class);
        dispatchHandler.execute(copyData);
    }

    @Override
    public boolean removeByCondition(AsyncCmdExecutorBaseBO entity) {
        checkBaseParam(entity);
        return asyncCmdService.removeByCondition(entity.getBizType(), entity.getEventType(), entity.getBizNumber(), true);
    }

    private void checkBaseParam(AsyncCmdExecutorBaseBO entity) {
        if (CharSequenceUtil.isBlank(entity.getBizType())) {
            throw new BizException(BizCodeEnum.PARAM_MISS, new Object[]{"bizType"});
        }
        if (CharSequenceUtil.isBlank(entity.getEventType())) {
            throw new BizException(BizCodeEnum.PARAM_MISS, new Object[]{"eventType"});
        }
        if (CharSequenceUtil.isBlank(entity.getBizNumber())) {
            throw new BizException(BizCodeEnum.PARAM_MISS, new Object[]{"bizNumber"});
        }
    }

    private void checkSubmitParam(AsyncCmdExecutorBO entity) {
        checkBaseParam(entity);
        if (MapUtil.isEmpty(entity.getContent())) {
            throw new BizException(BizCodeEnum.PARAM_MISS, new Object[]{"content"});
        }
        if (Objects.isNull(entity.getExecutorClass())) {
            throw new BizException(BizCodeEnum.PARAM_MISS, new Object[]{"executorClass"});
        }
    }

}
