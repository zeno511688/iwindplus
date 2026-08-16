/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.service.asynccmd.video;

import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdExecuteResultEnum;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdSubVO;
import com.iwindplus.base.async.cmd.domain.vo.AsyncCmdVO;
import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 视频合成示例：主任务收尾执行器.
 *
 * <p>组任务模式下，子任务全部成功后执行本收尾业务</p>
 *
 * @author zengdegui
 * @since 2026/8/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoDemoMergeTaskHandler implements AsyncCmdTaskHandler {

    @Override
    public AsyncCmdExecuteResultEnum execute(AsyncCmdVO entity) {
        // 组任务收尾：entity.getSubTasks() 携带全部成功的子任务结果
        final List<AsyncCmdSubVO> subTasks = entity.getSubTasks();
        log.info("videoDemo merge task final execute, id={} bizNumber={} subTaskCount={}",
            entity.getId(), entity.getBizNumber(),
            subTasks == null ? 0 : subTasks.size());
        return AsyncCmdExecuteResultEnum.SUCCESS;
    }

    @Override
    public void onTaskSuccess(AsyncCmdVO entity) {
        log.info("videoDemo merge task success, id={} bizNumber={}",
            entity.getId(), entity.getBizNumber());
    }

    @Override
    public void onTaskDiscard(AsyncCmdVO entity) {
        // 重试次数耗尽丢弃时的补偿/告警出口
        log.warn("videoDemo merge task discard, id={} bizNumber={}",
            entity.getId(), entity.getBizNumber());
    }
}
