/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.jobhandler;

import cn.hutool.core.date.DatePattern;
import com.iwindplus.base.document.domain.enums.DocumentTaskJobEnum;
import com.iwindplus.base.document.factory.DocumentTaskJobHandlerStrategyFactory;
import com.iwindplus.base.util.DatesUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档任务定时任务.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Slf4j
@RequiredArgsConstructor
public class DocumentTaskJob {

    private final DocumentTaskJobHandlerStrategyFactory factory;

    /**
     * 文档任务.
     */
    @XxlJob("documentTask")
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

        XxlJobHelper.log("文档任务，参数={}，开始时间={}，分片索引={}, 分片总数={}", jobParam, start, shardIndex, shardTotal);

        int failedJobs = 0;
        final DocumentTaskJobEnum[] jobEnums = DocumentTaskJobEnum.values();
        for (DocumentTaskJobEnum entity : jobEnums) {
            try {
                factory.getJobHandler(entity).execute(shardIndex, shardTotal);
            } catch (Exception e) {
                failedJobs++;

                log.error("文档任务={}失败", entity.getDesc(), e);
                XxlJobHelper.log("文档任务={}失败", entity.getDesc(), e);
            }
        }

        if (failedJobs > 0) {
            XxlJobHelper.handleFail("文档任务部分失败，失败个数=" + failedJobs);
            return;
        }

        final long endTimeMillis = System.currentTimeMillis();
        XxlJobHelper.log("文档任务，总任务个数={}, 结束时间={}，总执行毫秒数={}", jobEnums.length,
            DatesUtil.parseDate(endTimeMillis, DatePattern.NORM_DATETIME_MS_PATTERN), endTimeMillis - beginMillis);

        XxlJobHelper.handleSuccess();
    }
}
