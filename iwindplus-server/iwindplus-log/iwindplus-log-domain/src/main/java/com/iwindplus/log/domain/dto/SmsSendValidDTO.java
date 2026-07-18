/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 短信发送校验对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "短信发送校验对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendValidDTO implements Serializable {

    /**
     * 短信模板编码.
     */
    @Schema(description = "短信模板编码")
    @NotBlank(message = "{tplCode.notEmpty}")
    @Length(max = 100, message = "{tplCode.length}")
    private String tplCode;

    /**
     * 用户主键.
     */
    @NotNull(message = "{userId.notEmpty}")
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 组织主键.
     */
    @NotNull(message = "{orgId.notEmpty}")
    @Schema(description = "组织主键")
    private Long orgId;

    /**
     * 限制每天发送次数.
     */
    @Schema(description = "限制每天发送次数")
    private Integer limitCountDay;

    /**
     * 限制每小时发送次数.
     */
    @Schema(description = "限制每小时发送次数")
    private Integer limitCountHour;

    /**
     * 限制分钟发送次数.
     */
    @Schema(description = "限制每分钟发送次数")
    private Integer limitCountMinute;
}