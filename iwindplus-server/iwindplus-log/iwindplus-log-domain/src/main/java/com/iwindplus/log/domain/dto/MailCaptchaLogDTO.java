/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import com.iwindplus.base.domain.dto.DbBaseTwoDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 邮箱验证码日志.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "邮箱验证码日志对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailCaptchaLogDTO extends DbBaseTwoDTO {

    /**
     * 请求唯一标识.
     */
    @Schema(description = "请求唯一标识")
    @Length(max = 100, message = "{requestId.length}", groups = {SaveGroup.class, EditGroup.class})
    private String requestId;

    /**
     * 业务流水号.
     */
    @Schema(description = "业务流水号")
    @Length(max = 100, message = "{bizNumber.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizNumber;

    /**
     * 邮件模板编码.
     */
    @Schema(description = "邮件模板编码")
    @NotBlank(message = "{tplCode.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{tplCode.length}", groups = {SaveGroup.class, EditGroup.class})
    private String tplCode;

    /**
     * 邮箱.
     */
    @Schema(description = "邮箱")
    @NotBlank(message = "{mail.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{mail.length}", groups = {SaveGroup.class, EditGroup.class})
    private String mail;

    /**
     * 验证码.
     */
    @Schema(description = "验证码")
    @Length(max = 200, message = "{captcha.length}", groups = {SaveGroup.class, EditGroup.class})
    private String captcha;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 是否使用（false：未使用，true：已使用）
     */
    @Schema(description = "是否使用过（false：未使用，true：已使用）")
    private Boolean used;

    /**
     * 使用时间.
     */
    @Schema(description = "使用时间")
    private LocalDateTime useTime;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}