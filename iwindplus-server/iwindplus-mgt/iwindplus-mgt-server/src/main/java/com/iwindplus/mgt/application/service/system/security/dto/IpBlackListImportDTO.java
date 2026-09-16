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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * IP黑名单导入数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "IP黑名单导入数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
public class IpBlackListImportDTO extends ExcelImportResultBaseVO {

    /**
     * 名称.
     */
    @NotBlank(message = "{ip.notEmpty}")
    @ExcelProperty(value = {"*IP"}, index = 0)
    @ColumnWidth(value = 20)
    @Schema(description = "IP")
    private String ip;
}