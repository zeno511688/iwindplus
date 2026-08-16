/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynccmd.video;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdCallbackResultEnum;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdExecuteResultEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 视频合成示例：提交第三方合成子任务执行器（调第三方，提交后等回调）.
 *
 * <p>提交时executeSub返回ASYNC_WAIT进入异步等待，
 * 等待业务通过AsyncCmdExecutor.callback预存回调结果（优先），
 * 或轮询调用executeSubCallback主动查询第三方状态</p>
 *
 * @author zengdegui
 * @since 2026/8/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoDemoSubmitSubHandler implements AsyncCmdSubTaskHandler {

    @Override
    public AsyncCmdExecuteResultEnum executeSub(AsyncCmdSubVO entity) {
        final Map<String, Object> param = entity.getParam();
        final String videoId = String.valueOf(param.get("videoId"));
    
        // 示例：调用第三方合成接口，用entity.getBizNumber()作为第三方请求流水号
        // 第三方处理完成后回调业务接口，业务再调用AsyncCmdExecutor.callback上报
        log.info("videoDemo submit third subTask execute, subId={} bizNumber={} videoId={}",
            entity.getId(), entity.getBizNumber(), videoId);
    
        // 已发起第三方异步调用，显式返回ASYNC_WAIT进入异步等待
        return AsyncCmdExecuteResultEnum.ASYNC_WAIT;
    }

    /**
     * 轮询查询第三方状态（兜底）.
     * <p>回调预存结果由框架优先消费，仅当预存结果不存在时才调用本方法</p>
     */
    @Override
    public AsyncCmdCallbackResultEnum executeSubCallback(AsyncCmdSubVO entity) {
        // 示例：实际场景调用第三方查询接口，返回SUCCESS/FAILED/WAITING
        log.info("videoDemo submit third subTask poll callback, subId={} bizNumber={}",
            entity.getId(), entity.getBizNumber());
        return AsyncCmdCallbackResultEnum.WAITING;
    }

    @Override
    public void onSubTaskAsyncWait(AsyncCmdSubVO entity) {
        log.info("videoDemo submit third subTask asyncWait, subId={} bizNumber={}",
            entity.getId(), entity.getBizNumber());
    }
}
