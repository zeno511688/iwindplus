/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.vo;

import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.flow.domain.enums.FlowInstanceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 我的发起分页视图对象.
 *
 * @author zengdegui
 * @since 2026/05/20 23:41
 */
@Schema(description = "我的发起分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FlowHisInstancePageVO extends DbVersionBaseVO {

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

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
     * 耗时（毫秒）.
     */
    @Schema(description = "耗时（毫秒）")
    private Long takeTime;

    /**
     * 模型主键.
     */
    @Schema(description = "模型主键")
    private Long modelId;
}
