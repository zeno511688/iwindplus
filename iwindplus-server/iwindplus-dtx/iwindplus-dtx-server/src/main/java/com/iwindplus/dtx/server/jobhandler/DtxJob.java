/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.jobhandler;

import cn.hutool.core.date.DatePattern;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.dtx.domain.enums.DtxJobEnum;
import com.iwindplus.dtx.server.factory.DtxJobStrategyFactory;
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
@JobExecutor(name = "dtxJob")
public class DtxJob {

    @Resource
    private DtxJobStrategyFactory factory;

    /**
     * 分布式事务任务（重试/超时）.
     *
     * @param jobArgs 任务参数
     * @return ExecuteResult
     */
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        long beginMillis = System.currentTimeMillis();
        log.info("分布式事务任务，参数={}，开始时间={}", jobArgs.getJobParams()
            , DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN));

        final DtxJobEnum[] jobEnums = DtxJobEnum.values();
        for (DtxJobEnum job : jobEnums) {
            factory.getJobHandler(job).execute(0);
        }

        final long endTimeMillis = System.currentTimeMillis();
        log.info("全局事务任务，总任务个数={}, 结束时间={}，总执行毫秒数={}", jobEnums.length,
            DatesUtil.parseDate(endTimeMillis, DatePattern.NORM_DATETIME_MS_PATTERN), endTimeMillis - beginMillis);
        return ExecuteResult.success(true);
    }
}
