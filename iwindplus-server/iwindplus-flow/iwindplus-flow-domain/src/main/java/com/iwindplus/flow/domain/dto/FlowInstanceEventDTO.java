/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.flow.domain.enums.FlowInstanceEventTypeEnum;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程实例事件数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowInstanceEventDTO implements Serializable {

    /**
     * 事件类型.
     */
    private FlowInstanceEventTypeEnum eventType;

    /**
     * 实例主键.
     */
    private Long instanceId;

    /**
     * 实例名称.
     */
    private String instanceName;

    /**
     * 实例编码.
     */
    private String instanceCode;

    /**
     * 业务流水号.
     */
    private String bizNumber;

    /**
     * 回调地址.
     */
    private String callbackUrl;

    /**
     * 操作人主键.
     */
    private Long operatorId;

    /**
     * 操作人.
     */
    private String operatorName;
}
