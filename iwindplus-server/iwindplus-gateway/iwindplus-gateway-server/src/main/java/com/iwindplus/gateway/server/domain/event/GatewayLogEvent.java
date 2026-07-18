/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.gateway.server.domain.event;

import com.iwindplus.log.domain.dto.GatewayLogDTO;
import java.util.Map;
import lombok.Data;

/**
 * 网关日志事件.
 *
 * @author zengdegui
 * @since 2025/03/22 01:08
 */
@Data
public class GatewayLogEvent {

    private Map<String, String> mdcSnapshot;

    private GatewayLogDTO logData;

    /**
     * 复制数据.
     *
     * @param data 数据
     * @param mdc  MDC
     */
    public void copy(GatewayLogDTO data, Map<String, String> mdc) {
        this.logData = data;
        this.mdcSnapshot = mdc;
    }

    /**
     * 清空数据.
     */
    public void clear() {
        logData = null;
    }
}
