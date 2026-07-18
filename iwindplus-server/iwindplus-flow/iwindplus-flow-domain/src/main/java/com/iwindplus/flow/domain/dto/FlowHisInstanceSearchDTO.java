/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.flow.domain.enums.FlowInstanceQueryTypeEnum;
import com.iwindplus.flow.domain.enums.FlowInstanceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 我的发起搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:19
 */
@Schema(description = "我的发起搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowHisInstanceSearchDTO extends DbPageDTO {

    /**
     * 实例名称.
     */
    @Schema(description = "实例名称")
    private String name;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 实例状态.
     */
    @Schema(description = "实例状态（APPROVAL：审批中，AUDITED：已审核，REJECTED：已驳回，REVOKED：已撤销，TERMINATED：已终止）")
    private FlowInstanceStatusEnum status;

    /**
     * 模型名称.
     */
    @Schema(description = "模型名称")
    private String modelName;

    /**
     * 用户主键
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 查询类型.
     */
    @Schema(description = "查询类型（MY_INITIATED：我的发起，MY_DONE：我的已办，MY_CC：抄送我的）")
    private FlowInstanceQueryTypeEnum queryType;
}
