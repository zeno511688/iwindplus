/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.jobhandler;

import cn.hutool.core.date.DatePattern;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdJobEnum;
import com.iwindplus.base.async.cmd.factory.AsyncCmdJobHandlerStrategyFactory;
import com.iwindplus.base.util.DatesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令.
 *
 * @author zengdegui
 * @since 2024/11/19 01:42
 */
@Slf4j
@JobExecutor(name = "asyncCmdJob")
@RequiredArgsConstructor
public class AsyncCmdJob {

    private final AsyncCmdJobHandlerStrategyFactory factory;

    /**
     * 异步命令.
     *
     * @param jobArgs 任务参数
     * @return ExecuteResult
     */
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        long beginMillis = System.currentTimeMillis();

        log.info("异步命令，参数={}，开始时间={}", jobArgs.getJobParams()
            , DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN));

        final AsyncCmdJobEnum[] jobEnums = AsyncCmdJobEnum.values();
        for (AsyncCmdJobEnum entity : jobEnums) {
            factory.getJobHandler(entity).execute(0);
        }

        final long endTimeMillis = System.currentTimeMillis();
        log.info("异步命令，总任务个数={}, 结束时间={}，总执行毫秒数={}", jobEnums.length,
            DatesUtil.parseDate(endTimeMillis, DatePattern.NORM_DATETIME_MS_PATTERN), endTimeMillis - beginMillis);
        return ExecuteResult.success(true);
    }
}
