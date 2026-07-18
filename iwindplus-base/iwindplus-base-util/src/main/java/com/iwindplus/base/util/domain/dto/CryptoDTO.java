/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.dto;

import com.iwindplus.base.domain.enums.AlgorithmTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 加解密安全配置数据传输对象.
 *
 * @author zengdegui
 * @since 2025/04/24 00:00
 */
@Schema(description = "加解密安全配置数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoDTO implements Serializable {

    /**
     * 是否启用.
     */
    @Schema(description = "是否启用")
    private Boolean enabled;

    /**
     * 算法.
     */
    @Schema(description = "算法")
    private AlgorithmTypeEnum algorithm;

    /**
     * 公钥.
     */
    @Schema(description = "公钥")
    private String publicKey;

    /**
     * 私钥.
     */
    @Schema(description = "私钥")
    private String privateKey;

    /**
     * 密钥.
     */
    @Schema(description = "密钥")
    private String key;
}
