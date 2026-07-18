/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.base.binlog.domain.event;

import com.iwindplus.base.binlog.domain.dto.BinlogDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * binlog日志事件.
 *
 * @author zengdegui
 * @since 2025/03/22 01:08
 */
@Getter
public class BinLogEvent extends ApplicationEvent {

    private BinlogDTO logData;

    public BinLogEvent(Object source, BinlogDTO logData) {
        super(source);
        this.logData = logData;
    }
}
