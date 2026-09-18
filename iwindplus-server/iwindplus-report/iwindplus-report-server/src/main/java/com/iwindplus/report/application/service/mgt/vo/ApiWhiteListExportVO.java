/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.report.application.service.mgt.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API白名单导出数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "API白名单导出数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApiWhiteListExportVO implements Serializable {

    /**
     * 名称.
     */
    @ExcelProperty("名称")
    @ColumnWidth(value = 20)
    @Schema(description = "名称")
    private String name;

    /**
     * API路径.
     */
    @ColumnWidth(value = 20)
    @ExcelProperty("API路径")
    @Schema(description = "API路径")
    private String apiUrl;
}