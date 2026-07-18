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
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * excel导入结果数据传输对象.
 *
 * @author zengdegui
 * @since 2024/06/30 16:09
 */
@Schema(description = "excel导入结果数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResultDTO implements Serializable {

    /**
     * 行号.
     */
    @Schema(description = "行号")
    private Integer rowNum;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    private String errorMsg;
}
