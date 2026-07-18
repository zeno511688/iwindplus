/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 微信小程序配置编辑数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "微信小程序配置编辑数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class WechatConfigMaEditDTO extends WechatConfigMaDTO {

}