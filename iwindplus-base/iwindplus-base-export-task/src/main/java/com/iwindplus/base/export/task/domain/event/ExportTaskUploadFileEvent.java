/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.event;

import com.iwindplus.base.export.task.domain.vo.ExportTaskVO;
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

    private ExportTaskVO entity;

    public ExportTaskUploadFileEvent(Object source, ExportTaskVO entity) {
        super(source);
        this.entity = entity;
    }
}
