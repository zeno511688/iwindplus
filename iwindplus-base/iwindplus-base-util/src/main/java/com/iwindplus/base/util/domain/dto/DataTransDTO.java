/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 数据传输对象.
 *
 * @author zengdegui
 * @since 2025/04/24 00:00
 */
@Schema(description = "数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DataTransDTO implements Serializable {

    /**
     * 数据.
     */
    @Schema(description = "数据")
    private Object data;

}
