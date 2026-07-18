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
 * 数据库签名生成数据传输对象.
 *
 * @author zengdegui
 * @since 2025/11/20 23:38
 */
@Schema(description = "数据库签名生成数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DbSignGenerateDTO implements Serializable {

    /**
     * 密钥.
     */
    @Schema(description = "密钥")
    private String secretKey;

    /**
     * 加签盐.
     */
    @Schema(description = "加签盐")
    private Long salt;

    /**
     * 数据库名.
     */
    @Schema(description = "数据库名")
    private String dbName;

    /**
     * 表名.
     */
    @Schema(description = "表名")
    private String tableName;

    /**
     * 操作类型.
     */
    @Schema(description = "操作类型")
    private String action;
}
