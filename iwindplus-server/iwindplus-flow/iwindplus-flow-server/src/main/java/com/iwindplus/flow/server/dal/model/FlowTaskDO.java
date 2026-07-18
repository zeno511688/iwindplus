/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.flow.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.flow.domain.enums.FlowTaskTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程任务表.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Schema(description = "流程任务实体对象")
@TableName(value = "`flow_task`")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowTaskDO extends DbBaseDO {

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 节点编码.
     */
    @Schema(description = "节点编码")
    private String nodeCode;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 类型.
     */
    @Schema(description = "类型")
    private FlowTaskTypeEnum type;

    /**
     * 审批意见.
     */
    @Schema(description = "审批意见")
    private String comment;

    /**
     * 实例主键.
     */
    @Schema(description = "实例主键")
    private Long instanceId;

    /**
     * 模型主键.
     */
    @Schema(description = "模型主键")
    private Long modelId;
}
