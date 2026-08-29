/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.support.impl;

import com.iwindplus.base.document.domain.dto.ExportTaskShardSearchDTO;
import com.iwindplus.base.document.domain.enums.DocumentTaskJobEnum;
import com.iwindplus.base.document.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.document.domain.property.DocumentProperty;
import com.iwindplus.base.document.domain.vo.ExportTaskVO;
import com.iwindplus.base.document.service.ExportTaskService;
import com.iwindplus.base.document.support.ExportTaskBizProcessor;
import com.iwindplus.base.document.support.ExportTaskStateSupport;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 异步命令定时任务处理器.
 *
 * @author zengdegui
 * @since 2025/11/29 23:12
 */
@Slf4j
public class ExportTaskJobHandler extends AbstractExportTaskJobHandler {

    private final ExportTaskBizProcessor exportTaskBizProcessor;
    private final ExportTaskStateSupport exportTaskStateSupport;

    public ExportTaskJobHandler(
        DocumentProperty property,
        ExportTaskService asyncCmdService,
        ExportTaskBizProcessor exportTaskBizProcessor,
        ExportTaskStateSupport exportTaskStateSupport) {
        super(property, asyncCmdService);
        this.exportTaskBizProcessor = exportTaskBizProcessor;
        this.exportTaskStateSupport = exportTaskStateSupport;
    }

    @Override
    public DocumentTaskJobEnum support() {
        return DocumentTaskJobEnum.EXPORT_EXCEL_JOB;
    }

    @Override
    protected boolean doExecute(List<ExportTaskVO> entityList) {
        int dispatched = 0;
        int skipped = 0;
        int failed = 0;
        boolean poolFull = false;

        for (ExportTaskVO entity : entityList) {
            if (this.shouldSkip(entity)) {
                skipped++;
                continue;
            }

            if (!this.exportTaskBizProcessor.execute(entity)) {
                failed++;

                log.warn("重试导出任务投递被拒，共享池已满，已投递={}/{} id={}",
                    dispatched, entityList.size(), entity.getId());

                poolFull = true;
                break;
            }
            dispatched++;
        }

        if (dispatched > 0 || skipped > 0 || failed > 0) {
            log.info("重试导出任务完成，调度成功的={}, 跳过的={}, 失败的={}", dispatched, skipped, failed);
        }

        // 共享池满时返回true，避免execute方法提前结束整个捞取过程
        return poolFull || dispatched > 0;
    }

    @Override
    protected ExportTaskShardSearchDTO buildJobSearchDTO() {
        // 查询所有未完成任务，具体是否到调度时间在 shouldSkip 中按状态判断。
        return ExportTaskShardSearchDTO.builder()
            .statusList(ExportTaskStatusEnum.getUnfinishedStatus())
            .build();
    }

    @Override
    protected boolean shouldSkip(ExportTaskVO entity) {
        final ExportTaskStatusEnum status = entity.getStatus();
        final long currentTime = System.currentTimeMillis();

        if (ExportTaskStatusEnum.EXECUTING.equals(status)) {
            final Long expireTime = entity.getExpireTime();
            if (expireTime == null || expireTime > currentTime) {
                return true;
            }

            if (this.exportTaskStateSupport.taskExecutingToPending(entity)) {
                return false;
            }

            log.debug("导出任务已被其他实例处理，跳过重新调度，id={}", entity.getId());
            return true;
        }

        final Long nextRetryTime = entity.getNextRetryTime();
        if (nextRetryTime != null && nextRetryTime > currentTime) {
            log.debug("导出任务尚未到重试时间，跳过调度，id={}, status={}, nextRetryTime={}",
                entity.getId(), status, nextRetryTime);
            return true;
        }

        return false;
    }
}
