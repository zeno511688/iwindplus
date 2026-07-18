/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.flow.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程实例表.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Schema(description = "流程实例实体对象")
@TableName(value = "`flow_instance`")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowInstanceDO extends DbBaseDO {

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 当前节点编码.
     */
    @Schema(description = "当前节点编码")
    private String currentNodeCode;

    /**
     * 当前节点名称
     */
    @Schema(description = "当前节点名称")
    private String currentNodeName;

    /**
     * 模型主键.
     */
    @Schema(description = "模型主键")
    private Long modelId;
}
