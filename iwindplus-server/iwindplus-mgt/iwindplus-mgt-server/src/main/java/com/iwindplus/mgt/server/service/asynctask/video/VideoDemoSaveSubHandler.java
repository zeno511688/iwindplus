/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynctask.video;

import com.iwindplus.base.async.task.domain.vo.AsyncTaskExecuteResultVO;
import com.iwindplus.base.async.task.domain.vo.AsyncTaskSubVO;
import com.iwindplus.base.async.task.support.AsyncTaskSubHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 视频合成示例：结果转存子任务执行器（后续任务，读取前置子任务结果）.
 *
 * @author zengdegui
 * @since 2026/8/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoDemoSaveSubHandler implements AsyncTaskSubHandler {

    @Override
    public AsyncTaskExecuteResultVO executeSub(AsyncTaskSubVO entity) {
        final Map<String, Object> param = entity.getParam();
        final String videoId = String.valueOf(param.get("videoId"));

        // 示例：读取前置批次中第三方合成子任务（bizType=VIDEO_SUBMIT）的回调结果
        final Map<String, Object> submitResult = entity.getPriorSubTaskResult("VIDEO_SUBMIT", Map.class);
        log.info("videoDemo save subTask execute, subId={} videoId={} submitResult={}",
            entity.getId(), videoId, submitResult);
        return AsyncTaskExecuteResultVO.success();
    }
}
