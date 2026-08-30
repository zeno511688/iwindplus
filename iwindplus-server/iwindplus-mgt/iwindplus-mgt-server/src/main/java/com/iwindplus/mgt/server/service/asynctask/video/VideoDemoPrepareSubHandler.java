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
 * 视频合成示例：素材准备子任务执行器（本地任务，同步执行成功）.
 *
 * @author zengdegui
 * @since 2026/8/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoDemoPrepareSubHandler implements AsyncTaskSubHandler {

    @Override
    public AsyncTaskExecuteResultVO executeSub(AsyncTaskSubVO entity) {
        final Map<String, Object> param = entity.getParam();
        final String videoId = String.valueOf(param.get("videoId"));

        // 示例：本地素材准备业务，抛异常即子任务失败进入重试链
        log.info("videoDemo prepare subTask execute, subId={} bizNumber={} videoId={}",
            entity.getId(), entity.getBizNumber(), videoId);
        return AsyncTaskExecuteResultVO.success();
    }

    @Override
    public void onSubTaskSuccess(AsyncTaskSubVO entity) {
        log.info("videoDemo prepare subTask success, subId={} bizNumber={}",
            entity.getId(), entity.getBizNumber());
    }
}
