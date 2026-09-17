/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.iwindplus.base.domain.constant.CommonConstant.FileConstant;
import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.DbPageVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.export.task.domain.constant.ExportTaskConstant;
import com.iwindplus.base.export.task.domain.dto.ExportTaskStatusEditDTO;
import com.iwindplus.base.export.task.domain.enums.ExportTaskCodeEnum;
import com.iwindplus.base.export.task.domain.enums.ExportTaskStatusEnum;
import com.iwindplus.base.export.task.domain.property.ExportTaskProperty;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.export.task.factory.ExportTaskHandlerFactory;
import com.iwindplus.base.export.task.service.ExportTaskService;
import com.iwindplus.base.oss.domain.dto.OssCloudUploadDTO;
import com.iwindplus.base.oss.factory.OssExecuteHandlerFactory;
import com.iwindplus.base.oss.support.OssExecuteHandler;
import com.iwindplus.base.util.JacksonUtil;
import java.io.File;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

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
    ObjectProvider<OssExecuteHandlerFactory> ossExecuteHandlerFactoryProvider) {

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

        try (ExcelWriter excelWriter = EasyExcel.write(tempFilePath, rowClass).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet(handler.getSheetName()).build();
            DbPageDTO queryPageDTO = this.buildQueryPageDTO(handler, task, batchSize);

            // 写入第一页数据
            DbPageVO<?> dataPage = handler.pageByCondition(queryPageDTO);
            Long exportedCount = this.writeFirstPage(excelWriter, writeSheet, handler, dataPage, task);
            if (exportedCount == null) {
                return;
            }

            // 写入剩余页面数据
            this.writeRemainingPages(excelWriter, writeSheet, handler, queryPageDTO, dataPage, task, exportedCount);
        }

        // 导出成功，回写文件路径供下载使用
        this.resolveFilePath(task, tempFilePath);
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

        final OssExecuteHandlerFactory factory = this.ossExecuteHandlerFactoryProvider.getIfAvailable();
        if (factory == null) {
            log.warn("exportTask oss enabled but OssExecuteHandlerFactory not available, fallback to local file. id={}", task.getId());
            task.setFilePath(tempFilePath);
            return;
        }

        final OssExecuteHandler ossHandler = this.resolveOssHandler(factory, ossConfig.getCode());
        if (ossHandler == null) {
            log.warn("exportTask oss handler not found, fallback to local file. id={}", task.getId());
            task.setFilePath(tempFilePath);
            return;
        }

        try {
            final String relativePath = this.buildOssRelativePath(ossConfig, task.getFileName());
            final OssCloudUploadDTO uploadDTO = OssCloudUploadDTO.builder()
                .bucketName(ossConfig.getBucketName())
                .accessDomain(ossConfig.getAccessDomain())
                .data(FileUtil.readBytes(tempFilePath))
                .sourceFileName(task.getFileName())
                .relativePath(relativePath)
                .renamed(Boolean.TRUE)
                .returnAbsolutePath(Boolean.FALSE)
                .build();
            final UploadVO uploadVO = ossHandler.uploadFile(uploadDTO);
            if (uploadVO != null) {
                task.setFilePath(uploadVO.getRelativePath());
                log.info("exportTask upload to oss success. id={} filePath={}", task.getId(), uploadVO.getRelativePath());
            } else {
                log.warn("exportTask upload to oss failed, fallback to local file. id={}", task.getId());
                task.setFilePath(tempFilePath);
            }
        } catch (Exception ex) {
            log.error("exportTask upload to oss error, fallback to local file. id={}", task.getId(), ex);
            task.setFilePath(tempFilePath);
        } finally {
            // 上传完成后删除本地临时文件
            FileUtil.del(tempFilePath);
        }
    }

    /**
     * 构建OSS相对路径.
     *
     * @param ossConfig OSS配置
     * @param fileName  文件名
     * @return 相对路径
     */
    private String buildOssRelativePath(ExportTaskProperty.OssConfig ossConfig, String fileName) {
        final String prefix = Optional.ofNullable(ossConfig.getRelativePathPrefix())
            .filter(CharSequenceUtil::isNotBlank)
            .orElse(ExportTaskConstant.OSS_RELATIVE_PATH_PREFIX);
        return prefix.endsWith(ExportTaskConstant.PATH_SEPARATOR)
            ? prefix + fileName
            : prefix + ExportTaskConstant.PATH_SEPARATOR + fileName;
    }

    /**
     * 解析OSS策略.
     *
     * <p>优先使用配置编码匹配的策略，未指定编码时使用默认策略。</p>
     *
     * @param factory OSS策略工厂
     * @param code    配置编码
     * @return OSS策略
     */
    private OssExecuteHandler resolveOssHandler(OssExecuteHandlerFactory factory, String code) {
        if (CharSequenceUtil.isBlank(code)) {
            return factory.getDefaultHandler();
        }
        return factory.getHandler(property.getOss().getType(), code);
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
     * @param dataPage    数据分页
     * @param task        导出任务
     * @return 已导出数量，如果无数据则返回null
     */
    private Long writeFirstPage(ExcelWriter excelWriter,
        WriteSheet writeSheet, ExportTaskHandler handler, DbPageVO<?> dataPage, ExportTaskVO task) {
        List<?> dataList = dataPage.getRecords();
        if (CollUtil.isEmpty(dataList)) {
            return null;
        }

        excelWriter.write(this.convertToRowList(handler, dataList), writeSheet);
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
        DbPageDTO queryPageDTO, DbPageVO<?> firstDataPage, ExportTaskVO task, Long exportedCount) {
        Long totalPages = firstDataPage.getPages();

        for (Long currentPage = ExportTaskConstant.SECOND_PAGE_INDEX; currentPage <= totalPages; currentPage++) {
            queryPageDTO.setCurrent(currentPage);
            DbPageVO<?> dataPage = handler.pageByCondition(queryPageDTO);
            List<?> dataList = dataPage.getRecords();

            if (CollUtil.isEmpty(dataList)) {
                break;
            }

            excelWriter.write(this.convertToRowList(handler, dataList), writeSheet);
            exportedCount += dataList.size();

            // 更新进度
            this.updateProgress(task, firstDataPage.getTotal(), exportedCount);
        }
    }

    /**
     * 将分页查询结果转换为导出行数据.
     *
     * <p>分页查询返回的是业务视图对象，与Excel行模型（getRowClass）可能不一致，
     * 直接写入会导致EasyExcel按实际类型查找Converter失败，因此统一转换为行模型后再写入。</p>
     *
     * @param handler  导出任务助手
     * @param dataList 分页查询结果
     * @return 导出行数据
     */
    private List<?> convertToRowList(ExportTaskHandler handler, List<?> dataList) {
        final Class<?> rowClass = this.getRowClass(handler);
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
     * 更新导出进度.
     *
     * @param task          导出任务
     * @param totalCount    总数
     * @param exportedCount 已导出数量
     */
    private void updateProgress(ExportTaskVO task, Long totalCount, Long exportedCount) {
        int progress = (int) ((exportedCount * ExportTaskConstant.PROGRESS_PERCENT_BASE) / totalCount);
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
