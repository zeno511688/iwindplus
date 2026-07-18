/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.enums.OrgAuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 组织搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2021/1/31
 */
@Schema(description = "组织搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgSearchDTO extends DbPageDTO {
	/**
	 * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
	 */
	@Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
	private EnableStatusEnum status;

	/**
	 * 审核状态（NEW_BUILT：新建，UN_AUDITED：待审核，AUDITED：已审核，REJECTED：已驳回）.
	 */
	@Schema(description = "审核状态（NEW_BUILT：新建，UN_AUDITED：待审核，AUDITED：已审核，REJECTED：已驳回）")
	private OrgAuditStatusEnum auditStatus;

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
	 * 简称.
	 */
	@Schema(description = "简称")
	private String abbr;
}
