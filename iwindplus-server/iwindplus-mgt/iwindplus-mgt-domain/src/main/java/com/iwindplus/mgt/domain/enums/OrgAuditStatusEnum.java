/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 组织审核状态枚举.
 *
 * @author zengdegui
 * @since 2021/1/31
 */
@Getter
@RequiredArgsConstructor
public enum OrgAuditStatusEnum implements BaseEnum<Integer> {
	/**
	 * 新建.
	 */
	NEW_BUILT(0, "新建"),

	/**
	 * 待审核.
	 */
	UN_AUDITED(1, "待审核"),

	/**
	 * 已审核.
	 */
	AUDITED(2, "已审核"),

	/**
	 * 已驳回.
	 */
	REJECTED(3, "已驳回")
	;

	/**
	 * 值.
	 */
	@EnumValue
	private final Integer value;

	/**
	 * 描述.
	 */
	private final String desc;
}
