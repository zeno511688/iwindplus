/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.domain.dto;

import com.iwindplus.log.domain.dto.BinlogAlertDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * binlog 行数据处理传输对象.
 *
 * @author zengdegui
 * @since 2025/11/21 22:24
 */
@Schema(description = "binlog 行数据处理传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogRowDataProcessDTO implements Serializable {

    /**
     * 原数据.
     */
    @Schema(description = "原数据")
    private BinlogRowDataDTO sourceData;

    /**
     * binlog告警数据.
     */
    @Schema(description = "binlog告警数据")
    private  BinlogAlertDTO binlogAlert;
}
