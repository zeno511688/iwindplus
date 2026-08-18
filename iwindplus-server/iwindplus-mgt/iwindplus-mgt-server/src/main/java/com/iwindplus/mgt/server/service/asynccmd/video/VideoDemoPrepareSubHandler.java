/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynccmd.video;

import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdExecuteResultVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
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
public class VideoDemoPrepareSubHandler implements AsyncCmdSubTaskHandler {

    @Override
    public AsyncCmdExecuteResultVO executeSub(AsyncCmdSubVO entity) {
        final Map<String, Object> param = entity.getParam();
        final String videoId = String.valueOf(param.get("videoId"));

        // 示例：本地素材准备业务，抛异常即子任务失败进入重试链
        log.info("videoDemo prepare subTask execute, subId={} bizNumber={} videoId={}",
            entity.getId(), entity.getBizNumber(), videoId);
        return AsyncCmdExecuteResultVO.success();
    }

    @Override
    public void onSubTaskSuccess(AsyncCmdSubVO entity) {
        log.info("videoDemo prepare subTask success, subId={} bizNumber={}",
            entity.getId(), entity.getBizNumber());
    }
}
