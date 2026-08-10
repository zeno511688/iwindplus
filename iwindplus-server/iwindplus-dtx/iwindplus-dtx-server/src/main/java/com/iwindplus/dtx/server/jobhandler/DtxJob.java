/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.jobhandler;

import cn.hutool.core.date.DatePattern;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.dtx.domain.enums.DtxJobEnum;
import com.iwindplus.dtx.server.factory.DtxJobStrategyFactory;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 分布式事务任务.
 *
 * @author zengdegui
 * @since 2024/11/19 01:42
 */
@Slf4j
@ConditionalOnProperty(prefix = "dtx.job", name = "enabled", havingValue = "true", matchIfMissing = true)
@Component
public class DtxJob {

    @Resource
    private DtxJobStrategyFactory factory;

    /**
     * 分布式事务任务（重试/超时）.
     */
    @XxlJob("dtxJob")
    public void jobExecute() {
        final long beginMillis = System.currentTimeMillis();
        final String start = DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN);

        final int shardIndex = Math.max(XxlJobHelper.getShardIndex(), 0);
        final int shardTotal = Math.max(XxlJobHelper.getShardTotal(), 1);
        final String jobParam = XxlJobHelper.getJobParam();

        XxlJobHelper.log("分布式事务任务，参数={}，开始时间={}，分片索引={}, 分片总数={}", jobParam, start, shardIndex, shardTotal);

        int failedJobs = 0;
        final DtxJobEnum[] jobEnums = DtxJobEnum.values();
        for (DtxJobEnum entity : jobEnums) {
            try {
                factory.getJobHandler(entity).execute(shardIndex, shardTotal);
            } catch (Exception e) {
                failedJobs++;

                log.error("分布式事务任务={}失败", entity.getDesc(), e);
                XxlJobHelper.log("分布式事务任务={}失败", entity.getDesc(), e);
            }
        }

        if (failedJobs > 0) {
            XxlJobHelper.handleFail("分布式事务任务部分失败，失败个数=" + failedJobs);
            return;
        }

        final long endTimeMillis = System.currentTimeMillis();
        XxlJobHelper.log("分布式事务任务，总任务个数={}, 结束时间={}，总执行毫秒数={}", jobEnums.length,
            DatesUtil.parseDate(endTimeMillis, DatePattern.NORM_DATETIME_MS_PATTERN), endTimeMillis - beginMillis);

        XxlJobHelper.handleSuccess();
    }
}
