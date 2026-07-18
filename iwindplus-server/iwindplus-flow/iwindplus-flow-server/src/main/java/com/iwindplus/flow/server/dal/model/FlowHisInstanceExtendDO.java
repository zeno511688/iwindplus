/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 历史流程实例扩展表（处理大字段）.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "历史流程实例扩展对象")
@TableName(value = "`flow_his_instance_extend`", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class FlowHisInstanceExtendDO extends FlowInstanceExtendDO {

}
