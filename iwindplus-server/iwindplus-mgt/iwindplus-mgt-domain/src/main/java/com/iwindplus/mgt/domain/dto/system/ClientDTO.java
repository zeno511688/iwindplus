/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 客户端数据传输对象.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "客户端数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO extends DbVersionBaseDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 客户端名称.
     */
    @Schema(description = "客户端名称")
    @NotBlank(message = "{clientName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{clientName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String clientName;

    /**
     * 客户端密钥过期时间.
     */
    @Schema(description = "客户端密钥过期时间")
    private LocalDateTime clientSecretExpiresAt;

    /**
     * 客户端支持的认证方法.
     */
    @Schema(description = "认证方法")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<String> authenticationMethod;

    /**
     * 客户端支持的授权类型.
     */
    @Schema(description = "客户端支持的授权类型")
    @NotEmpty(message = "{authorizedGrantType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<String> authorizedGrantType;

    /**
     * 重定向地址.
     */
    @Schema(description = "重定向地址")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<String> redirectUri;

    /**
     * Openid Connect登出后跳转地址.
     */
    @Schema(description = "Openid Connect登出后跳转地址")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<String> logoutRedirectUri;

    /**
     * 客户端申请的权限范围.
     */
    @Schema(description = "客户端申请的权限范围")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<String> scope;

    /**
     * 客户端设置
     */
    @Schema(title = "客户端设置")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ClientSettingDTO clientSetting;

    /**
     * 客户端token设置
     */
    @Schema(title = "客户端token设置")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private TokenSettingDTO tokenSetting;
}
