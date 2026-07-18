/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.domain.dto;

import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * binlog 操作处理数据传输对象.
 *
 * @author zengdegui
 * @since 2025/11/29 23:09
 */
@Schema(description = "binlog 操作处理数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogActionProcessDTO<T> implements Serializable {

    /**
     * 操作类型
     */
    @Schema(description = "操作类型")
    private DbActionTypeEnum actionType;

    /**
     * 操作数据
     */
    @Schema(description = "操作数据")
    private T data;
}
