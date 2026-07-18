/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
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
 * 邮件配置数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/27
 */
@Schema(description = "邮件配置数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MailConfigDTO extends DbVersionBaseDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    @NotBlank(message = "{name.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{name.length}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 发件人昵称.
     */
    @Schema(description = "发件人昵称")
    @NotBlank(message = "{smtpNickName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{smtpNickName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String nickName;

    /**
     * 发件服务器主机.
     */
    @Schema(description = "发件服务器主机")
    @NotBlank(message = "{smtpHost.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{smtpHost.length}", groups = {SaveGroup.class, EditGroup.class})
    private String host;

    /**
     * 发件服务器账户.
     */
    @Schema(description = "发件服务器账户")
    @NotBlank(message = "{smtpUsername.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{smtpUsername.length}", groups = {SaveGroup.class, EditGroup.class})
    private String username;

    /**
     * 发件服务器密码.
     */
    @Schema(description = "发件服务器密码")
    @NotBlank(message = "{smtpPassword.notEmpty}", groups = {SaveGroup.class})
    @Length(max = 100, message = "{smtpPassword.length}", groups = {SaveGroup.class})
    private String password;

    /**
     * 发件服务器端口.
     */
    @Schema(description = "发件服务器端口")
    private Integer port;

    /**
     * 是否启用ssl（false：否，true：是）.
     */
    @Schema(description = "是否启用ssl（false：否，true：是）")
    private Boolean sslEnable;

    /**
     * 是否启用重试（false：否，true：是）.
     */
    @Schema(description = "是否启用重试（false：否，true：是）")
    private Boolean retryEnable;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
