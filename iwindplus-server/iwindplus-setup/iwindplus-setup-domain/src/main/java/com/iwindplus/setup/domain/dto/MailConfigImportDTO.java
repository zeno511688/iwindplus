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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 邮箱配置导入数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "邮箱配置导入数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
public class MailConfigImportDTO extends ExcelImportResultDTO {

    /**
     * 名称.
     */
    @NotBlank(message = "{name.notEmpty}")
    @ExcelProperty(value = {"*名称"}, index = 0)
    @ColumnWidth(value = 20)
    @Schema(description = "名称")
    private String name;

    /**
     * 发件人昵称.
     */
    @NotBlank(message = "{smtpNickName.notEmpty}")
    @ExcelProperty(value = {"*发件人昵称"}, index = 1)
    @ColumnWidth(value = 20)
    @Schema(description = "发件人昵称")
    private String nickName;

    /**
     * 发件服务器主机.
     */
    @NotBlank(message = "{smtpHost.notEmpty}")
    @ExcelProperty(value = {"*发件服务器主机"}, index = 2)
    @ColumnWidth(value = 25)
    @Schema(description = "发件服务器主机")
    private String host;

    /**
     * 发件服务器账户.
     */
    @NotBlank(message = "{smtpUsername.notEmpty}")
    @ExcelProperty(value = {"*发件服务器账户"}, index = 3)
    @ColumnWidth(value = 25)
    @Schema(description = "发件服务器账户")
    private String username;

    /**
     * 发件服务器密码.
     */
    @NotBlank(message = "{smtpPassword.notEmpty}")
    @ExcelProperty(value = {"*发件服务器密码"}, index = 4)
    @ColumnWidth(value = 25)
    @Schema(description = "发件服务器密码")
    private String password;

    /**
     * 发件服务器端口.
     */
    @NotBlank(message = "{smtpPort.notEmpty}")
    @ExcelProperty(value = {"*发件服务器端口"}, index = 5)
    @ColumnWidth(value = 25)
    @Schema(description = "发件服务器端口")
    private String port;

    /**
     * 是否启用ssl（false：否，true：是）.
     */
    @NotBlank(message = "{smtpSslEnable.notEmpty}")
    @ExcelProperty(value = {"*是否启用ssl"}, index = 6)
    @ColumnWidth(value = 10)
    @Schema(description = "是否启用ssl（false：否，true：是）")
    private String sslEnable;

    /**
     * 是否启用重试（false：否，true：是）.
     */
    @NotBlank(message = "{smtpRetryEnable.notEmpty}")
    @ExcelProperty(value = {"*是否启用重试"}, index = 7)
    @ColumnWidth(value = 10)
    @Schema(description = "是否启用重试（false：否，true：是）")
    private String retryEnable;

    /**
     * 备注.
     */
    @ExcelProperty(value = {"备注"}, index = 8)
    @ColumnWidth(value = 20)
    @Schema(description = "备注")
    private String remark;
}