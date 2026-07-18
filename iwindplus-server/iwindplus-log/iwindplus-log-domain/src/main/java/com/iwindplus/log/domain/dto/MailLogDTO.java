/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import com.iwindplus.base.domain.dto.DbBaseTwoDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 邮件日志.
 *
 * @author zengdegui
 * @since 2020/4/27
 */
@Schema(description = "邮件日志对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailLogDTO extends DbBaseTwoDTO {

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
    private String bizNumber;

    /**
     * 主题.
     */
    @Schema(description = "主题")
    @NotBlank(message = "{subject.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 255, message = "{moduleName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String subject;

    /**
     * 内容（必填）.
     */
    @Schema(description = "内容")
    @NotBlank(message = "{content.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private String content;

    /**
     * 发件服务器账户.
     */
    @Schema(description = "发件服务器账户")
    @NotBlank(message = "{username.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{username.length}", groups = {SaveGroup.class, EditGroup.class})
    private String username;

    /**
     * 发件人昵称.
     */
    @Schema(description = "发件人昵称")
    @NotBlank(message = "{nickName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{nickName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String nickName;

    /**
     * 收件人（必填）.
     */
    @Schema(description = "收件人")
    @NotBlank(message = "{tos.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private String tos;

    /**
     * 抄送人.
     */
    @Schema(description = "抄送人")
    private String ccs;

    /**
     * 密送人.
     */
    @Schema(description = "密送人")
    private String bccs;

    /**
     * 发送次数.
     */
    @Schema(description = "发送次数")
    private Integer sendCount;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 发送结果（0：失败，1：成功）.
     */
    @Schema(description = "发送结果（false：失败，true：成功）")
    private Boolean result;

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
