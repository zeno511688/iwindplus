/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.dto;

import com.iwindplus.base.domain.enums.SensitiveTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 脱敏配置数据传输对象.
 *
 * @author zengdegui
 * @since 2025/04/24 00:00
 */
@Schema(description = "脱敏配置数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveDTO implements Serializable {

    /**
     * 脱敏类型.
     */
    @Schema(description = "脱敏类型")
    private SensitiveTypeEnum type;

    /**
     * 脱敏开始位置.
     */
    @Schema(description = "脱敏开始位置")
    private Integer startInclude;

    /**
     * 脱敏末尾保留位数.
     */
    @Schema(description = "脱敏末尾保留位数")
    private Integer endReserve;
}
