/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.iwindplus.base.domain.dto.ExcelImportResultDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对象存储配置导入数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "对象存储配置导入数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
public class OssConfigImportDTO extends ExcelImportResultDTO {

    /**
     * 名称.
     */
    @NotBlank(message = "{name.notEmpty}")
    @ExcelProperty(value = {"*名称"}, index = 0)
    @ColumnWidth(value = 20)
    @Schema(description = "名称")
    private String name;

    /**
     * 类型.
     */
    @NotNull(message = "{ossType.notEmpty}")
    @ColumnWidth(value = 20)
    @ExcelProperty(value = {"*类型"}, index = 1)
    @Schema(description = "类型")
    private String type;

    /**
     * oss地域节点（必填）.
     */
    @ColumnWidth(value = 20)
    @ExcelProperty(value = {"oss地域节点"}, index = 2)
    @Schema(description = "oss地域节点")
    private String ossEndpoint;

    /**
     * 访问key.
     */
    @NotBlank(message = "{accessKey.notEmpty}")
    @ExcelProperty(value = {"*访问key"}, index = 3)
    @ColumnWidth(value = 20)
    @Schema(description = "访问key")
    private String accessKey;

    /**
     * 密匙.
     */
    @NotBlank(message = "{secretKey.notEmpty}")
    @ExcelProperty(value = {"*密匙"}, index = 4)
    @ColumnWidth(value = 20)
    @Schema(description = "密匙")
    private String secretKey;

    /**
     * sts地域节点（可选）.
     */
    @ExcelProperty(value = {"sts地域节点"}, index = 5)
    @ColumnWidth(value = 20)
    @Schema(description = "sts地域节点")
    private String stsEndpoint;

    /**
     * RAM角色（可选）.
     */
    @ExcelProperty(value = {"RAM角色"}, index = 6)
    @ColumnWidth(value = 20)
    @Schema(description = "RAM角色")
    private String roleArn;

    /**
     * 备注.
     */
    @ExcelProperty(value = {"备注"}, index = 7)
    @ColumnWidth(value = 20)
    @Schema(description = "备注")
    private String remark;
}