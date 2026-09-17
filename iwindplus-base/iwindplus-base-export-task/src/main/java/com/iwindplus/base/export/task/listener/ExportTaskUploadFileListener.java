/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.listener;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.domain.vo.UploadVO;
import com.iwindplus.base.export.task.domain.constant.ExportTaskConstant;
import com.iwindplus.base.export.task.domain.dto.ExportTaskUploadFileDTO;
import com.iwindplus.base.export.task.domain.event.ExportTaskUploadFileEvent;
import com.iwindplus.base.export.task.domain.property.ExportTaskProperty;
import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.util.domain.enums.FileTypeEnum;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

/**
 * 导出任务上传文件监听器.
 *
 * @author zengdegui
 * @since 2025/03/21 21:51
 */
@Slf4j
@RequiredArgsConstructor
public class ExportTaskUploadFileListener {

    private final ExportTaskProperty property;
    private final HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory;

    /**
     * 导出任务上传文件.
     *
     * @param event 事件
     */
    @EventListener(ExportTaskUploadFileEvent.class)
    public void onApplicationEvent(ExportTaskUploadFileEvent event) {
        final ExportTaskVO entity = event.getEntity();
        if (Objects.isNull(entity)) {
            log.warn("导出任务上传文件数据为空");
            return;
        }

        final ExportTaskProperty.OssConfig cfg = property.getOss();
        final String relativePath = this.buildOssRelativePath(cfg, entity.getFileName());
        // 保存本地临时文件路径，避免回写OSS相对路径后无法删除本地文件
        final String localFilePath = entity.getFilePath();

        try {
            // 发布上传文件事件
            final ExportTaskUploadFileDTO uploadParam = ExportTaskUploadFileDTO.builder()
                .code(cfg.getCode())
                .tplCode(cfg.getTplCode())
                .data(FileUtil.readBytes(localFilePath))
                .relativePath(relativePath)
                .sourceFileName(entity.getFileName())
                .contentType(FileTypeEnum.XLSX.getContentType())
                .renamed(Boolean.TRUE)
                .build();

            final ResultVO<UploadVO> response = httpClientExecuteHandlerFactory
                .getDefaultHandler()
                .post(
                    cfg.getUploadUrl(),
                    uploadParam,
                    null,
                    new TypeReference<>() {
                    }
                );
            response.errorThrow();
            // 上传成功后回写相对路径，供 taskSuccess 保存进数据库
            Optional.ofNullable(response.getBizData()).ifPresent(uploadVO -> entity.setFilePath(uploadVO.getRelativePath()));
            log.info("{} 导出任务上传文件成功", SpringUtil.getApplicationName());
        } finally {
            // 上传完成后删除本地临时文件
            FileUtil.del(localFilePath);
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
}
