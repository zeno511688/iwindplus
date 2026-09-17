/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.event;

import com.iwindplus.base.domain.dto.FileBaseDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 导出任务上传文件事件.
 *
 * @author zengdegui
 * @since 2025/03/22 01:08
 */
@Getter
public class ExportTaskUploadFileEvent extends ApplicationEvent {

    private FileBaseDTO entity;

    public ExportTaskUploadFileEvent(Object source, FileBaseDTO entity) {
        super(source);
        this.entity = entity;
    }
}
