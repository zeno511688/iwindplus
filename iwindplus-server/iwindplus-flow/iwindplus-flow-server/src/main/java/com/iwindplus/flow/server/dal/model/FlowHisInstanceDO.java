/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.flow.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.flow.domain.enums.FlowInstanceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 历史流程实例表.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Schema(description = "历史流程实例实体对象")
@TableName(value = "`flow_his_instance`")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowHisInstanceDO extends FlowInstanceDO {

    /**
     * 实例状态.
     */
    @Schema(description = "实例状态")
    private FlowInstanceStatusEnum status;

    /**
     * 耗时.
     */
    @Schema(description = "耗时")
    private Long takeTime;

}
