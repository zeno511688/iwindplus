/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.jobhandler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.model.dto.ExecuteResult;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.flow.domain.enums.FlowInstanceCallbackStatusEnum;
import com.iwindplus.flow.server.dal.model.FlowInstanceCallbackDO;
import com.iwindplus.flow.server.dal.repository.FlowInstanceCallbackRepository;
import com.iwindplus.flow.server.service.FlowInstanceCallbackService;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 流程实例回调任务.
 * <p>
 * 负责处理待处理(PENDING)和失败(FAILED)状态的回调记录，进行重试。 最大重试次数为3次，超过后状态更新为丢弃(DISCARD)。
 *
 * @author zengdegui
 * @since 2024/11/19 01:42
 */
@Slf4j
@Component
@JobExecutor(name = "flowInstanceCallback")
public class FlowInstanceCallbackJob {

    @Resource
    private FlowInstanceCallbackRepository flowInstanceCallbackRepository;

    @Resource
    private FlowInstanceCallbackService flowInstanceCallbackService;

    /**
     * 流程实例回调重试任务.
     *
     * @param jobArgs 任务参数
     * @return ExecuteResult
     */
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        long beginMillis = System.currentTimeMillis();

        log.info("流程实例回调重试任务，参数={}，开始时间={}", jobArgs.getJobParams(),
            DatesUtil.parseDate(beginMillis, DatePattern.NORM_DATETIME_MS_PATTERN));

        try {
            // 查询待处理和失败的回调记录
            List<FlowInstanceCallbackDO> callbacks = flowInstanceCallbackRepository.list(
                Wrappers.lambdaQuery(FlowInstanceCallbackDO.class)
                    .in(FlowInstanceCallbackDO::getStatus,
                        FlowInstanceCallbackStatusEnum.PENDING,
                        FlowInstanceCallbackStatusEnum.FAILED)
                    .orderByAsc(FlowInstanceCallbackDO::getCreatedTime)
                    .last("LIMIT 100")
            );

            if (CollUtil.isEmpty(callbacks)) {
                log.info("流程实例回调重试任务，没有需要处理的回调记录");
                return ExecuteResult.success(true);
            }

            log.info("流程实例回调重试任务，找到{}条待处理记录", callbacks.size());

            int successCount = 0;
            int failedCount = 0;

            for (FlowInstanceCallbackDO callback : callbacks) {
                try {
                    boolean result = flowInstanceCallbackService.executeCallback(callback);
                    if (result) {
                        successCount++;
                    } else {
                        failedCount++;
                    }
                } catch (Exception e) {
                    failedCount++;
                    log.error("流程实例回调处理异常 callbackId={}", callback.getId(), e);
                }
            }

            long endTimeMillis = System.currentTimeMillis();
            log.info("流程实例回调重试任务完成，成功={}, 失败={}, 总耗时={}ms",
                successCount, failedCount, endTimeMillis - beginMillis);

            return ExecuteResult.success(failedCount == 0);

        } catch (Exception e) {
            log.error("流程实例回调重试任务执行异常", e);
            return ExecuteResult.failure(e.getMessage());
        }
    }
}
