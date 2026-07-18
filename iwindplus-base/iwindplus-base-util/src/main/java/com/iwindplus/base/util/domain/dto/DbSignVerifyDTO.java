/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 数据库签名验证数据传输对象.
 *
 * @author zengdegui
 * @since 2025/11/20 23:38
 */
@Schema(description = "数据库签名验证数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DbSignVerifyDTO extends DbSignGenerateDTO {

    /**
     * 签名.
     */
    @Schema(description = "签名")
    private String sign;
}
