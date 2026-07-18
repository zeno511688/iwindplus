/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 应用凭证基础视图对象.
 *
 * @author zengdegui
 * @since 2019/2/15
 */
@Schema(description = "应用凭证基础视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AppCertBaseVO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

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
}
