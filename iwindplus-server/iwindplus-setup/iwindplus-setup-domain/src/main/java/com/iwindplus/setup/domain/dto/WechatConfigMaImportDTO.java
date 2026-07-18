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
 * 微信小程序配置导入数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "微信小程序配置导入数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
public class WechatConfigMaImportDTO extends ExcelImportResultDTO {

    /**
     * 名称.
     */
    @NotBlank(message = "{name.notEmpty}")
    @ExcelProperty(value = {"*名称"}, index = 0)
    @ColumnWidth(value = 20)
    @Schema(description = "名称")
    private String name;

    /**
     * 小程序的appId.
     */
    @NotBlank(message = "{wechatMaAppId.notEmpty}")
    @ExcelProperty(value = {"*小程序appId"}, index = 1)
    @ColumnWidth(value = 20)
    @Schema(description = "小程序appId")
    private String accessKey;

    /**
     * 小程序密钥.
     */
    @NotBlank(message = "{wechatMaSecretKey.notEmpty}")
    @ExcelProperty(value = {"*小程序密钥"}, index = 2)
    @ColumnWidth(value = 20)
    @Schema(description = "小程序密钥")
    private String secretKey;

    /**
     * 消息推送token.
     */
    @NotBlank(message = "{token.notEmpty}")
    @ExcelProperty(value = {"*消息推送token"}, index = 3)
    @ColumnWidth(value = 30)
    @Schema(description = "消息推送token")
    private String token;

    /**
     * 消息推送加密密钥.
     */
    @NotBlank(message = "{aesKey.notEmpty}")
    @ExcelProperty(value = {"*消息推送加密密钥"}, index = 4)
    @ColumnWidth(value = 30)
    @Schema(description = "消息推送加密密钥")
    private String aesKey;

    /**
     * 消息推送数据格式，XML或者JSON.
     */
    @NotBlank(message = "{msgDataFormat.notEmpty}")
    @ExcelProperty(value = {"*消息推送数据格式"}, index = 5)
    @ColumnWidth(value = 20)
    @Schema(description = "消息推送数据格式，XML或者JSON")
    private String msgDataFormat;

    /**
     * 备注.
     */
    @ExcelProperty(value = {"备注"}, index = 6)
    @ColumnWidth(value = 20)
    @Schema(description = "备注")
    private String remark;
}