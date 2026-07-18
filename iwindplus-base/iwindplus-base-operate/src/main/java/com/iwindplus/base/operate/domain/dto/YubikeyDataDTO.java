/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * yubikey数据传输对象.
 *
 * @author zengdegui
 * @since 2024/4/10
 */
@Schema(description = "yubikey数据传输对象")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class YubikeyDataDTO implements Serializable {

    /**
     * 原数据.
     */
    @Schema(description = "原数据")
    private String source;

    /**
     * 签名数据.
     */
    @Schema(description = "签名数据")
    private String sign;
}
