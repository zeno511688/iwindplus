/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.jobhandler;

import cn.hutool.core.date.DatePattern;
import com.iwindplus.base.async.cmd.domain.enums.AsyncCmdJobEnum;
import com.iwindplus.base.async.cmd.factory.AsyncCmdJobHandlerStrategyFactory;
import com.iwindplus.base.util.DatesUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令任务.
 *
 * @author zengdegui
 * @since 2024/11/19 01:42
 */
@Slf4j
@RequiredArgsConstructor
public class AsyncCmdJob {

    private final AsyncCmdJobHandlerStrategyFactory factory;

    /**
     * 异步命令任务.
     */
    @XxlJob("asyncCmdJob")
    public void jobExecute() {
        final long beginMillis = System.currentTimeMillis();
        final String start = DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN);

        // 获取分片参数，并进行边界校验
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        // 边界校验：确保分片参数合法
        if (shardTotal < 1) {
            shardTotal = 1;
        }
        if (shardIndex < 0 || shardIndex >= shardTotal) {
            log.warn("分片索引异常，shardIndex={}, shardTotal={}，已自动修正为0", shardIndex, shardTotal);
            shardIndex = 0;
        }

        final String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("异步命令任务，参数={}，开始时间={}，分片索引={}, 分片总数={}", jobParam, start, shardIndex, shardTotal);

        int failedJobs = 0;
        final AsyncCmdJobEnum[] jobEnums = AsyncCmdJobEnum.values();
        for (AsyncCmdJobEnum entity : jobEnums) {
            try {
                factory.getJobHandler(entity).execute(shardIndex, shardTotal);
            } catch (Exception e) {
                failedJobs++;

                log.error("异步命令任务={}失败", entity.getDesc(), e);
                XxlJobHelper.log("异步命令任务={}失败", entity.getDesc(), e);
            }
        }

        if (failedJobs > 0) {
            XxlJobHelper.handleFail("异步命令任务部分失败，失败个数=" + failedJobs);
            return;
        }

        final long endTimeMillis = System.currentTimeMillis();
        XxlJobHelper.log("异步命令任务，总任务个数={}, 结束时间={}，总执行毫秒数={}", jobEnums.length,
            DatesUtil.parseDate(endTimeMillis, DatePattern.NORM_DATETIME_MS_PATTERN), endTimeMillis - beginMillis);

        XxlJobHelper.handleSuccess();
    }
}
