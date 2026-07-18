/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 用户绑定授权结果视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "用户绑定授权结果视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserBindResultVO implements Serializable {
	/**
	 * 用户主键.
	 */
	@Schema(description = "用户主键")
	private Long userId;

	/**
	 * 是否绑定手机.
	 */
	@Schema(description = "是否绑定手机")
	private Boolean bindMobileFlag;
}
