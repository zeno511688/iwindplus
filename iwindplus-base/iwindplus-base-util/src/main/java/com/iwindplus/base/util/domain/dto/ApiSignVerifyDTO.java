/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API签名验证数据传输对象.
 *
 * @author zengdegui
 * @since 2025/04/24 00:00
 */
@Schema(description = "API签名验证数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSignVerifyDTO extends ApiSignGenerateDTO {

    /**
     * 签名.
     */
    @Schema(description = "签名")
    private String sign;

    /**
     * 签名超时时间.
     */
    @Schema(description = "签名超时时间")
    private Duration timeout;
}
