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
 * 流程实例扩展表（处理大字段）.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "流程实例扩展对象")
@TableName(value = "`flow_instance_extend`", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowInstanceExtendDO extends DbBaseDO {

    /**
     * 变量
     */
    @Schema(description = "变量")
    private String variable;

    /**
     * 实例主键.
     */
    @Schema(description = "实例主键")
    private Long instanceId;
}
