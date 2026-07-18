/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.annotation.EnumValid;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.mgt.domain.enums.OrgAuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 组织审核数据传输对象.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "组织审核数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgAuditDTO extends DbVersionBaseDTO {

    /**
     * 审核状态（NEW_BUILT：新建，UN_AUDITED：待审核，AUDITED：已审核，REJECTED：已驳回）.
     */
    @Schema(description = "审核状态（NEW_BUILT：新建，UN_AUDITED：待审核，AUDITED：已审核，REJECTED：已驳回）")
    @NotNull(message = "{auditStatus.notEmpty}", groups = {SaveGroup.class})
    @EnumValid(message = "{auditStatus.illegal}", clazz = OrgAuditStatusEnum.class, groups = {SaveGroup.class})
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
    @NotNull(message = "{orgId.notEmpty}", groups = {SaveGroup.class})
    private Long orgId;
}
