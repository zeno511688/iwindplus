/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 基础签名视图对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "基础签名视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseSignVO implements Serializable {

    /**
     * 访问key.
     */
    @Schema(description = "访问key")
    private String accessKey;

    /**
     * 密钥.
     */
    @Schema(description = "密钥")
    private String secretKey;

    /**
     * 签名超时时间.
     */
    @Schema(description = "签名超时时间（单位：秒）")
    private Integer timeout;
}
