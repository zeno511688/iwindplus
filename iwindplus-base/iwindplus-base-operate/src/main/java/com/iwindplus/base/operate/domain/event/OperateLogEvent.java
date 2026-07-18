/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate.domain.event;

import com.iwindplus.base.operate.domain.dto.OperateLogDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 操作日志事件.
 *
 * @author zengdegui
 * @since 2025/03/22 01:08
 */
@Getter
public class OperateLogEvent extends ApplicationEvent {

    private OperateLogDTO operateLogData;

    public OperateLogEvent(Object source, OperateLogDTO operateLogData) {
        super(source);
        this.operateLogData = operateLogData;
    }
}
