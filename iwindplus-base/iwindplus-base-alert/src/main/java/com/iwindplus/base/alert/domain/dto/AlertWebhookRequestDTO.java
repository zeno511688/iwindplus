/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.alert.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Webhook 告警请求数据传输对象
 *
 * @author zengdegui
 * @since 2026/03/03 19:34
 */
@Schema(description = "Webhook告警请求DTO")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class AlertWebhookRequestDTO extends AlertBaseRequestDTO {
}
