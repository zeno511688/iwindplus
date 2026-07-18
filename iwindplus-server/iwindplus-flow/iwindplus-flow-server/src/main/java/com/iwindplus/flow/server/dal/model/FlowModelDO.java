/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.flow.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.flow.domain.enums.FlowFormTypeEnum;
import com.iwindplus.flow.domain.enums.FlowModelStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程模型表.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Schema(description = "流程模型对象")
@TableName(value = "`flow_model`")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowModelDO extends DbBaseDO {

    /**
     * 状态（TO_BE_PUBLISHED：待发布，PUBLISHED：已发布，DISABLED：已停用，HISTORY：历史版本）.
     */
    @Schema(description = "状态（TO_BE_PUBLISHED：待发布，PUBLISHED：已发布，DISABLED：已停用，HISTORY：历史版本）")
    private FlowModelStatusEnum status;

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
     * 模型版本
     */
    @Schema(description = "模型版本")
    private Integer modelVersion;

    /**
     * 表单类型（NONE：无表单，FORM：表单，CUSTOM：自定义，FIXED：固定格式）.
     */
    @Schema(description = "表单类型")
    private FlowFormTypeEnum formType;

    /**
     * 表单路径（模型类型为自定义时用）
     */
    @Schema(description = "表单路径")
    private String formPath;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 分类主键.
     */
    @Schema(description = "分类主键")
    private Long categoryId;

    /**
     * 表单主键.
     */
    @Schema(description = "表单主键")
    private Long formId;
}
