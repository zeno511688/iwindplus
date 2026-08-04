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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
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

        final int shardIndex = Math.max(XxlJobHelper.getShardIndex(), 0);
        final int shardTotal = Math.max(XxlJobHelper.getShardTotal(), 1);
        final String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("异步命令，参数={}，开始时间={}，分片索引={}, 分片总数={}", jobParam, start, shardIndex, shardTotal);

        final AtomicInteger failed = new AtomicInteger(0);
        final AsyncCmdJobEnum[] jobEnums = AsyncCmdJobEnum.values();
        List<CompletableFuture<Void>> futures =
            Arrays.stream(jobEnums)
                .map(entity ->
                    CompletableFuture.runAsync(() -> {
                        try {
                            factory.getJobHandler(entity).execute(shardIndex, shardTotal);
                        } catch (Exception e) {
                            failed.incrementAndGet();

                            XxlJobHelper.log("异步命令任务={}失败", entity.getDesc(), e);
                        }
                    })
                ).toList();

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        if (failed.get() > 0) {
            XxlJobHelper.handleFail("异步命令部分失败，失败个数=" + failed.get());
            return;
        }

        final long endTimeMillis = System.currentTimeMillis();
        XxlJobHelper.log("异步命令，总任务个数={}, 结束时间={}，总执行毫秒数={}", jobEnums.length,
            DatesUtil.parseDate(endTimeMillis, DatePattern.NORM_DATETIME_MS_PATTERN), endTimeMillis - beginMillis);

        XxlJobHelper.handleSuccess();
    }
}
