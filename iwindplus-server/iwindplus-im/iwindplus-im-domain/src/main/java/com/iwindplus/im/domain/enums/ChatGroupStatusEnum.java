/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 聊天群状态枚举.
 *
 * @author zengdegui
 * @since 2021/1/31
 */
@Getter
@RequiredArgsConstructor
public enum ChatGroupStatusEnum implements BaseEnum<Integer> {
	/**
	 * 封禁.
	 */
	BAN(0, "封禁"),

	/**
	 * 正常.
	 */
	NORMAL(1, "正常"),
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
