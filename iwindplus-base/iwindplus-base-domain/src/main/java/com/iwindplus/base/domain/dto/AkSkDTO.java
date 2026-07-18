/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 通用 AccessKey/SecretKey.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AkSkDTO implements Serializable {

    /**
     * 访问key（必填）.
     */
    @Schema(description = "访问key")
    private String accessKey;

    /**
     * 密钥（必填）.
     */
    @Schema(description = "密钥")
    private String secretKey;
}
