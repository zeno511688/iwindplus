/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.application.service.system.security.dto;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.iwindplus.base.domain.vo.ExcelImportResultBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API白名单导入数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "API白名单导入数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
public class ApiWhiteListImportDTO extends ExcelImportResultBaseVO {

    /**
     * 名称.
     */
    @NotBlank(message = "{name.notEmpty}")
    @ExcelProperty(value = {"*名称"}, index = 0)
    @ColumnWidth(value = 20)
    @Schema(description = "名称")
    private String name;

    /**
     * API路径.
     */
    @NotNull(message = "{apiUrl.notEmpty}")
    @ColumnWidth(value = 20)
    @ExcelProperty(value = {"*API路径"}, index = 1)
    @Schema(description = "API路径")
    private String apiUrl;
}