/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.mgt.application.service.system.security.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * IP黑名单导出数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "IP黑名单导出数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IpBlackListExportVO implements Serializable {

    /**
     * 名称.
     */
    @ExcelProperty("IP")
    @ColumnWidth(value = 20)
    @Schema(description = "IP")
    private String ip;
}