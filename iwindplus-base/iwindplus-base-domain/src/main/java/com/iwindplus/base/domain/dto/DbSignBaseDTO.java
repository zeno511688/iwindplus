/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 数据库加签字段数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "数据库加签字段数据传输对象")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DbSignBaseDTO extends DbBaseDTO {

    /**
     * 加签盐.
     */
    @Schema(description = "加签盐")
    private Long salt;

    /**
     * 签名（防篡改）
     */
    @Schema(description = "签名")
    private String sign;
}
