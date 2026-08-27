/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.jobhandler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.util.DatesUtil;
import com.iwindplus.flow.domain.dto.FlowInstanceCallbackShardSearchDTO;
import com.iwindplus.flow.domain.enums.FlowInstanceCallbackStatusEnum;
import com.iwindplus.flow.server.dal.model.FlowInstanceCallbackDO;
import com.iwindplus.flow.server.service.FlowInstanceCallbackService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 流程实例回调任务.
 * <p>
 * 负责处理待处理(PENDING)和失败(FAILED)状态的回调记录，进行重试。
 * 最大重试次数由 flow.maxRetry 配置，超过后状态更新为丢弃(DISCARD)。
 *
 * @author zengdegui
 * @since 2024/11/19 01:42
 */
@Slf4j
@Component
public class FlowInstanceCallbackJob {

    @Resource
    private FlowInstanceCallbackService flowInstanceCallbackService;

    /**
     * 流程实例回调重试任务.
     */
    @XxlJob("flowInstanceCallback")
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

        XxlJobHelper.log("流程实例回调重试任务，参数={}，开始时间={}，分片索引={}, 分片总数={}", jobParam, start, shardIndex, shardTotal);

        this.execute(shardIndex, shardTotal);

        final long endTimeMillis = System.currentTimeMillis();
        XxlJobHelper.log("流程实例回调重试任务，结束时间={}，总执行毫秒数={}",
            DatesUtil.parseDate(endTimeMillis, DatePattern.NORM_DATETIME_MS_PATTERN),
            endTimeMillis - beginMillis);

        XxlJobHelper.handleSuccess();
    }

    /**
     * 执行job.
     *
     * @param shardIndex 分片索引
     * @param shardTotal 分片总数
     */
    private void execute(Integer shardIndex, Integer shardTotal) {
        final Integer size = this.flowInstanceCallbackService.getSize();
        if (Objects.isNull(size) || size <= 0) {
            log.error("每轮捞取条数={}, 本轮不捞取，请检查每页条数配置", size);
            return;
        }

        final FlowInstanceCallbackShardSearchDTO param = FlowInstanceCallbackShardSearchDTO.builder()
            .statusList(List.of(FlowInstanceCallbackStatusEnum.PENDING, FlowInstanceCallbackStatusEnum.FAILED))
            .build();
        param.setShardIndex(shardIndex);
        param.setShardTotal(shardTotal);
        param.setSize(size);

        long lastId = 0;
        int loop = 0;
        int total = 0;
        int successCount = 0;
        int failedCount = 0;

        while (loop < NumberConstant.NUMBER_ONE_HUNDRED) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            param.setLastId(lastId);

            final List<FlowInstanceCallbackDO> list = this.flowInstanceCallbackService.listByShard(param);
            if (CollUtil.isEmpty(list)) {
                break;
            }

            for (FlowInstanceCallbackDO callback : list) {
                try {
                    boolean result = this.flowInstanceCallbackService.executeCallback(callback);
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

            // 更新游标
            lastId = list.get(list.size() - 1).getId();
            loop++;
            total += list.size();
        }

        log.info("流程实例回调重试任务，分片={}/{} 轮次={}, 共处理【{}】条数据，成功={}, 失败={}",
            shardIndex, shardTotal, loop, total, successCount, failedCount);

        if (failedCount > 0) {
            XxlJobHelper.handleFail("流程实例回调重试任务部分失败，失败个数=" + failedCount);
        }
    }
}
