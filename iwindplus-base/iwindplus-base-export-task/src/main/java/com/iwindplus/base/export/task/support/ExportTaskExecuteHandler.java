/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.support;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskStatusEditDTO;
import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.factory.ExportTaskHandlerStrategyFactory;
import com.iwindplus.base.export.task.service.ExportTaskService;
import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.base.util.JacksonUtil;
import java.io.File;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * 导出任务执行助手（核心）.
 *
 * @author zengdegui
 * @since 2026/08/28 18:40
 */
@Slf4j
public record ExportTaskExecuteHandler(
    ExportTaskHandlerStrategyFactory exportTaskHandlerStrategyFactory,
    ExportTaskStateSupport exportTaskStateSupport,
    ExportTaskService exportTaskService) {

    /**
     * 执行导出任务.
     *
     * @param entity 导出任务实体
     */
    public void execute(ExportTaskVO entity) {
        ExportTaskHandler handler = this.getTaskHandler(entity.getExecuteName());
        final long start = System.currentTimeMillis();

        try {
            // 执行业务逻辑
            this.processTask(handler, entity);

            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            if (!this.exportTaskStateSupport().taskSuccess(entity, handler, costTime)) {
                log.warn("exportTask task execute success failed, id={}", entity.getId());
                return;
            }
        } catch (Exception ex) {
            log.error("exportTask task execute failed. id={}", entity.getId(), ex);

            // 失败
            final long costTime = Optional.ofNullable(entity.getCostTime()).orElse(0L) + System.currentTimeMillis() - start;
            this.exportTaskStateSupport.taskFail(entity, handler,
                costTime, ex, false);
        }
    }

    /**
     * 获取主任务助手.
     *
     * @param executeName 执行器名称
     * @return ExportTaskHandler
     */
    protected ExportTaskHandler getTaskHandler(String executeName) {
        return this.exportTaskHandlerStrategyFactory.getTaskHandler(executeName);
    }

    /**
     * 处理单个导出任务.
     *
     * @param handler 导出任务助手
     * @param task    导出任务
     */
    private void processTask(ExportTaskHandler handler, ExportTaskVO task) {
        String tempFilePath = this.buildTempFilePath(task.getFileName());
        int batchSize = 1000;

        try (ExcelWriter excelWriter = EasyExcel.write(tempFilePath, handler.getRowClass()).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet(handler.getSheetName()).build();
            DbPageDTO queryPageDTO = this.buildQueryPageDTO(handler, task, batchSize);

            // 写入第一页数据
            PageDTO<?> dataPage = handler.pageByCondition(queryPageDTO);
            Long exportedCount = this.writeFirstPage(excelWriter, writeSheet, dataPage, task);
            if (exportedCount == null) {
                return;
            }

            // 写入剩余页面数据
            this.writeRemainingPages(excelWriter, writeSheet, handler, queryPageDTO, dataPage, task, exportedCount);
        } finally {
            this.cleanupTempFile(tempFilePath);
        }
    }

    /**
     * 构建临时文件路径.
     *
     * @param fileName 文件名
     * @return 临时文件路径
     */
    private String buildTempFilePath(String fileName) {
        return System.getProperty("java.io.tmpdir") + File.separator + fileName;
    }

    /**
     * 构建查询参数.
     *
     * @param handler   导出任务助手
     * @param task      导出任务
     * @param batchSize 批次大小
     * @return 查询参数
     */
    private DbPageDTO buildQueryPageDTO(ExportTaskHandler handler, ExportTaskVO task, int batchSize) {
        DbPageDTO queryPageDTO = (DbPageDTO) JacksonUtil.convertValue(task.getQueryParam(), handler.getQueryClass());
        queryPageDTO.setCurrent(1);
        queryPageDTO.setSize(batchSize);
        return queryPageDTO;
    }

    /**
     * 写入第一页数据.
     *
     * @param excelWriter Excel写入器
     * @param writeSheet  写入工作表
     * @param dataPage    数据分页
     * @param task        导出任务
     * @return 已导出数量，如果无数据则返回null
     */
    private Long writeFirstPage(ExcelWriter excelWriter,
        WriteSheet writeSheet, PageDTO<?> dataPage, ExportTaskVO task) {
        List<?> dataList = dataPage.getRecords();
        if (CollUtil.isEmpty(dataList)) {
            return null;
        }

        excelWriter.write(dataList, writeSheet);
        Long exportedCount = (long) dataList.size();

        // 更新进度
        this.updateProgress(task, dataPage.getTotal(), exportedCount);

        return exportedCount;
    }

    /**
     * 写入剩余页面数据.
     *
     * @param excelWriter   Excel写入器
     * @param writeSheet    写入工作表
     * @param handler       导出任务助手
     * @param queryPageDTO  查询参数
     * @param firstDataPage 第一页数据
     * @param task          导出任务
     * @param exportedCount 已导出数量
     */
    private void writeRemainingPages(ExcelWriter excelWriter, WriteSheet writeSheet, ExportTaskHandler handler,
        DbPageDTO queryPageDTO, PageDTO<?> firstDataPage, ExportTaskVO task, Long exportedCount) {
        long totalPages = firstDataPage.getPages();

        for (int currentPage = 2; currentPage <= totalPages; currentPage++) {
            queryPageDTO.setCurrent(currentPage);
            PageDTO<?> dataPage = handler.pageByCondition(queryPageDTO);
            List<?> dataList = dataPage.getRecords();

            if (CollUtil.isEmpty(dataList)) {
                break;
            }

            excelWriter.write(dataList, writeSheet);
            exportedCount += dataList.size();

            // 更新进度
            this.updateProgress(task, null, exportedCount);
        }
    }

    /**
     * 清理临时文件.
     *
     * @param tempFilePath 临时文件路径
     */
    private void cleanupTempFile(String tempFilePath) {
        File tempFile = new File(tempFilePath);
        if (tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                log.warn("Failed to delete temp file: {}", tempFilePath);
            }
        }
    }

    /**
     * 更新导出进度.
     *
     * @param task          导出任务
     * @param totalCount    总数
     * @param exportedCount 已导出数量
     */
    private void updateProgress(ExportTaskVO task, Long totalCount, Long exportedCount) {
        int progress = (int) ((exportedCount * 100.0) / totalCount);
        final ExportTaskStatusEditDTO build = ExportTaskStatusEditDTO
            .builder()
            .id(task.getId())
            .from(ExportTaskStatusEnum.EXECUTING)
            .totalCount(totalCount)
            .exportedCount(exportedCount)
            .progress(progress)
            .build();
        this.exportTaskService.editStatusById(build);
    }
}
