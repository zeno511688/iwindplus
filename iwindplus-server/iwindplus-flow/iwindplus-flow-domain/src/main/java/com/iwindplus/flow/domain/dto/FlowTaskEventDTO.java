/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.flow.domain.enums.FlowTaskEventTypeEnum;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程任务事件数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowTaskEventDTO implements Serializable {

    /**
     * 事件类型.
     */
    private FlowTaskEventTypeEnum eventType;

    /**
     * 任务主键.
     */
    private Long taskId;

    /**
     * 任务名称.
     */
    private String taskName;

    /**
     * 操作人主键.
     */
    private Long operatorId;

    /**
     * 操作人.
     */
    private String operatorName;

    /**
     * 审批意见.
     */
    private String comment;

    /**
     * 新待办人主键列表（TASK_CREATED 时有值）.
     */
    private List<FlowNodePlayerDTO> assignees;
}
