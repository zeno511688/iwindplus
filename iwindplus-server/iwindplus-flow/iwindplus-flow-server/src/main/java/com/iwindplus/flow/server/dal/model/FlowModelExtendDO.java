/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.flow.domain.dto.FlowModelContentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 流程模型扩展表（处理大字段）.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "流程模型扩展对象")
@TableName(value = "`flow_model_extend`", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowModelExtendDO extends DbBaseDO {

    /**
     * 模型内容
     */
    @Schema(description = "模型内容")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FlowModelContentDTO modelContent;

    /**
     * 表单内容
     */
    @Schema(description = "表单内容")
    private String formContent;

    /**
     * 模型主键.
     */
    @Schema(description = "模型主键")
    private Long modelId;
}
