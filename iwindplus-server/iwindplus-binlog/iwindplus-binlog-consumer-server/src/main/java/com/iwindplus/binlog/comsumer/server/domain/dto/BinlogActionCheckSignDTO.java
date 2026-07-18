/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * binlog 验证签名数据传输对象.
 *
 * @author zengdegui
 * @since 2025/11/30 01:46
 */
@Schema(description = "binlog 验证签名数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogActionCheckSignDTO implements Serializable {

    /**
     * 是否成功.
     */
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 验证信息（成功不返回）.
     */
    private String message;
}
