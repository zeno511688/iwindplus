/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.iwindplus.base.domain.constant.CommonConstant.FileConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.base.domain.dto.FileBaseDTO;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.DbPageVO;
import com.iwindplus.base.export.task.domain.constant.ExportTaskConstant;
import com.iwindplus.base.export.task.domain.dto.ExportTaskExtDTO;
import com.iwindplus.base.export.task.domain.dto.ExportTaskStatusEditDTO;
import com.iwindplus.base.export.task.domain.enums.ExportTaskCodeEnum;
import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.export.task.domain.event.ExportTaskUploadFileEvent;
import com.iwindplus.base.export.task.domain.property.ExportTaskProperty;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.factory.ExportTaskHandlerFactory;
import com.iwindplus.base.export.task.service.ExportTaskService;
import com.iwindplus.base.util.JacksonUtil;
import java.io.File;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 导出任务执行助手（核心）.
 *
 * @author zengdegui
 * @since 2026/08/28 18:40
 */
@Slf4j
public record ExportTaskExecuteHandler(
    ExportTaskProperty property,
    ExportTaskHandlerFactory exportTaskHandlerFactory,
    ExportTaskStateSupport exportTaskStateSupport,
    ExportTaskService exportTaskService,
    ApplicationEventPublisher publisher) {

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
        return this.exportTaskHandlerFactory.getTaskHandler(executeName);
    }

    /**
     * 处理单个导出任务.
     *
     * @param handler 导出任务助手
     * @param task    导出任务
     */
    private void processTask(ExportTaskHandler handler, ExportTaskVO task) {
        final String fileName = handler.getFileName();
        task.setFileName(fileName);
        final String tempFilePath = this.buildTempFilePath(fileName);
        final Long batchSize = ExportTaskConstant.EXPORT_BATCH_SIZE;
        final Class<?> rowClass = this.getRowClass(handler);
        final long maxExportCount = this.resolveMaxExportCount(task);

        try (ExcelWriter excelWriter = EasyExcel.write(tempFilePath, rowClass).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet(handler.getSheetName()).build();
            DbPageDTO queryPageDTO = this.buildQueryPageDTO(handler, task, batchSize);

            // 写入第一页数据
            DbPageVO<?> dataPage = handler.pageByCondition(queryPageDTO);
            Long exportedCount = this.writeFirstPage(excelWriter, writeSheet, rowClass, dataPage, task, maxExportCount);
            if (exportedCount == null) {
                return;
            }

            // 写入剩余页面数据
            this.writeRemainingPages(excelWriter, writeSheet, handler, rowClass, queryPageDTO, dataPage, task, exportedCount, maxExportCount);
        }

        // 导出成功，回写文件路径供下载使用
        this.resolveFilePath(task, tempFilePath);
    }

    /**
     * 解析导出数据最大条数限制.
     *
     * @param task 导出任务
     * @return 最大条数限制
     */
    private long resolveMaxExportCount(ExportTaskVO task) {
        final Long maxExportCount = Optional.ofNullable(task.getExt())
            .map(ExportTaskExtDTO::getMaxExportCount)
            .orElse(null);
        return Optional.ofNullable(maxExportCount).orElse(this.property.getMaxExportCount());
    }

    /**
     * 解析导出文件最终访问路径.
     *
     * <p>启用OSS时上传到OSS并返回访问URL，否则使用本地文件路径。</p>
     *
     * @param task         导出任务
     * @param tempFilePath 本地临时文件路径
     */
    private void resolveFilePath(ExportTaskVO task, String tempFilePath) {
        final ExportTaskProperty.OssConfig ossConfig = this.property.getOss();
        if (ossConfig == null || Boolean.FALSE.equals(ossConfig.getEnabled())) {
            // 未启用OSS，使用本地文件路径
            task.setFilePath(tempFilePath);
            return;
        }

        // 发布上传文件事件
        final FileBaseDTO entity = FileBaseDTO.builder()
            .fileName(task.getFileName())
            .filePath(tempFilePath)
            .build();
        this.publisher.publishEvent(new ExportTaskUploadFileEvent(this, entity));
    }

    /**
     * 构建导出文件存储路径.
     *
     * <p>优先使用配置的存储目录，未配置时回退到系统临时目录。</p>
     *
     * @param fileName 文件名
     * @return 文件路径
     */
    private String buildTempFilePath(String fileName) {
        final String baseDir = System.getProperty(FileConstant.TMP_DIR);

        final File dir = new File(baseDir);
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("Failed to create export file directory: {}", baseDir);
        }

        return baseDir + File.separator + fileName;
    }

    /**
     * 获取Excel行模型类型.
     *
     * @param handler 导出任务助手
     * @return Excel行模型类型
     */
    private Class<?> getRowClass(ExportTaskHandler handler) {
        final Class<?> rowClass = handler.getRowClass();
        if (rowClass == null) {
            throw new BizException(ExportTaskCodeEnum.EXPORT_ROW_CLASS_NOT_CONFIGURED);
        }
        return rowClass;
    }

    /**
     * 构建查询参数.
     *
     * @param handler   导出任务助手
     * @param task      导出任务
     * @param batchSize 批次大小
     * @return 查询参数
     */
    private DbPageDTO buildQueryPageDTO(ExportTaskHandler handler, ExportTaskVO task, Long batchSize) {
        final Class<?> queryClass = handler.getQueryClass();
        if (queryClass == null) {
            throw new BizException(ExportTaskCodeEnum.EXPORT_QUERY_CLASS_NOT_CONFIGURED);
        }

        final Object queryParam = JacksonUtil.parseObject(task.getQueryParam(), queryClass);
        if (!(queryParam instanceof DbPageDTO queryPageDTO)) {
            throw new BizException(ExportTaskCodeEnum.EXPORT_QUERY_PARAM_PARSE_FAILED);
        }

        queryPageDTO.setCurrent(ExportTaskConstant.FIRST_PAGE_INDEX);
        queryPageDTO.setSize(batchSize);
        return queryPageDTO;
    }

    /**
     * 写入第一页数据.
     *
     * @param excelWriter Excel写入器
     * @param writeSheet  写入工作表
     * @param rowClass    Excel行模型类型
     * @param dataPage    数据分页
     * @param task        导出任务
     * @return 已导出数量，如果无数据则返回null
     */
    private Long writeFirstPage(ExcelWriter excelWriter,
        WriteSheet writeSheet, Class<?> rowClass, DbPageVO<?> dataPage, ExportTaskVO task, long maxExportCount) {
        List<?> dataList = dataPage.getRecords();
        if (CollUtil.isEmpty(dataList)) {
            return null;
        }

        // 截断超出最大条数限制的数据
        final List<?> limitedList = this.limitDataList(task, dataList, 0L, maxExportCount);
        if (CollUtil.isEmpty(limitedList)) {
            return null;
        }

        excelWriter.write(this.convertToRowList(rowClass, limitedList), writeSheet);
        Long exportedCount = (long) limitedList.size();

        // 更新进度
        this.updateProgress(task, dataPage.getTotal(), exportedCount, 0);

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
        Class<?> rowClass, DbPageDTO queryPageDTO, DbPageVO<?> firstDataPage, ExportTaskVO task, Long exportedCount, long maxExportCount) {
        Long totalPages = firstDataPage.getPages();
        final Long totalCount = firstDataPage.getTotal();
        int lastProgress = this.calcProgress(totalCount, exportedCount);

        for (Long currentPage = ExportTaskConstant.SECOND_PAGE_INDEX; currentPage <= totalPages; currentPage++) {
            queryPageDTO.setCurrent(currentPage);
            DbPageVO<?> dataPage = handler.pageByCondition(queryPageDTO);
            List<?> dataList = dataPage.getRecords();

            if (CollUtil.isEmpty(dataList)) {
                break;
            }

            // 截断超出最大条数限制的数据
            final List<?> limitedList = this.limitDataList(task, dataList, exportedCount, maxExportCount);
            if (CollUtil.isEmpty(limitedList)) {
                break;
            }

            excelWriter.write(this.convertToRowList(rowClass, limitedList), writeSheet);
            exportedCount += limitedList.size();

            // 更新进度（进度值变化时才写库）
            lastProgress = this.updateProgress(task, totalCount, exportedCount, lastProgress);

            // 达到最大条数限制，停止导出
            if (exportedCount >= maxExportCount) {
                break;
            }
        }
    }

    /**
     * 按最大条数限制截断数据列表.
     *
     * @param task          导出任务
     * @param dataList      原始数据列表
     * @param exportedCount 已导出数量
     * @param maxExportCount 最大条数限制
     * @return 截断后的数据列表
     */
    private List<?> limitDataList(ExportTaskVO task, List<?> dataList, long exportedCount, long maxExportCount) {
        if (maxExportCount <= 0) {
            return dataList;
        }
        final long remaining = maxExportCount - exportedCount;
        if (remaining <= 0) {
            return List.of();
        }
        if (remaining >= dataList.size()) {
            return dataList;
        }
        log.warn("exportTask export data exceeds max export count limit, truncated. id={} maxExportCount={} exportedCount={} total={}",
            task.getId(), maxExportCount, exportedCount, dataList.size());
        return dataList.subList(0, (int) remaining);
    }

    /**
     * 将分页查询结果转换为导出行数据.
     *
     * <p>分页查询返回的是业务视图对象，与Excel行模型（getRowClass）可能不一致，
     * 直接写入会导致EasyExcel按实际类型查找Converter失败，因此统一转换为行模型后再写入。</p>
     *
     * @param rowClass Excel行模型类型
     * @param dataList 分页查询结果
     * @return 导出行数据
     */
    private List<?> convertToRowList(Class<?> rowClass, List<?> dataList) {
        if (CollUtil.isEmpty(dataList)) {
            return dataList;
        }

        // 类型一致时无需转换
        if (rowClass.isInstance(dataList.get(0))) {
            return dataList;
        }

        return BeanUtil.copyToList(dataList, rowClass);
    }

    /**
     * 计算导出进度百分比.
     *
     * @param totalCount    总数
     * @param exportedCount 已导出数量
     * @return 进度百分比（0-100）
     */
    private int calcProgress(Long totalCount, Long exportedCount) {
        final long safeTotal = Optional.ofNullable(totalCount).orElse(0L);
        final long safeExported = Optional.ofNullable(exportedCount).orElse(0L);
        if (safeTotal <= 0) {
            return 0;
        }
        int progress = (int) ((safeExported * ExportTaskConstant.PROGRESS_PERCENT_BASE) / safeTotal);
        return Math.min(progress, NumberConstant.NUMBER_ONE_HUNDRED);
    }

    /**
     * 更新导出进度（仅当进度值发生变化时才写库，避免重复更新）.
     *
     * @param task           导出任务
     * @param totalCount     总数
     * @param exportedCount  已导出数量
     * @param lastProgress   上次已写入的进度值
     * @return 本次实际写入的进度值，未变化时返回上次进度值
     */
    private int updateProgress(ExportTaskVO task, Long totalCount, Long exportedCount, int lastProgress) {
        final int progress = this.calcProgress(totalCount, exportedCount);
        // 进度未变化时跳过写库，减少数据库往返
        if (progress == lastProgress) {
            return lastProgress;
        }
        final ExportTaskStatusEditDTO build = ExportTaskStatusEditDTO
            .builder()
            .id(task.getId())
            .from(ExportTaskStatusEnum.EXECUTING)
            .totalCount(totalCount)
            .exportedCount(exportedCount)
            .progress(progress)
            .build();
        this.exportTaskService.editStatusById(build);
        return progress;
    }
}
