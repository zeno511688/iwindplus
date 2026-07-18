/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.model.power;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.mgt.domain.enums.OrgAuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 组织审核表.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "组织审核对象")
@TableName(value = "`org_audit`")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrgAuditDO extends DbBaseDO {

    /**
     * 审核状态（NEW_BUILT：新建，UN_AUDITED：待审核，AUDITED：已审核，REJECTED：已驳回）.
     */
    @Schema(description = "审核状态（NEW_BUILT：新建，UN_AUDITED：待审核，AUDITED：已审核，REJECTED：已驳回）")
    private OrgAuditStatusEnum auditStatus;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
