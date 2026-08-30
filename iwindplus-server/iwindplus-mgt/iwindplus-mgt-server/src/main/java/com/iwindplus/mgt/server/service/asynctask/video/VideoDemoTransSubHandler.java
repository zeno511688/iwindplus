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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 视频合成示例：转码.
 *
 * @author zengdegui
 * @since 2026/8/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoDemoTransSubHandler implements AsyncTaskSubHandler {

    @Override
    public AsyncTaskExecuteResultVO executeSub(AsyncTaskSubVO entity) {
        return AsyncTaskExecuteResultVO.execute();
    }
}
