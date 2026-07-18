/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.mgt.domain.dto.system.ClientSettingDTO;
import com.iwindplus.mgt.domain.dto.system.TokenSettingDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 客户端视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "客户端视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientVO extends DbVersionBaseVO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 客户端id.
     */
    @Schema(description = "客户端id")
    private String clientId;

    /**
     * 客户端名称.
     */
    @Schema(description = "客户端名称")
    private String clientName;

    /**
     * 客户端签发时间.
     */
    @Schema(description = "客户端签发时间")
    private LocalDateTime clientIdIssuedAt;

    /**
     * 客户端密钥.
     */
    @Schema(description = "客户端密钥")
    private String clientSecret;

    /**
     * 客户端密钥过期时间.
     */
    @Schema(description = "客户端密钥过期时间")
    private LocalDateTime clientSecretExpiresAt;

    /**
     * 客户端支持的认证方法.
     */
    @Schema(description = "认证方法")
    private Set<String> authenticationMethod;

    /**
     * 客户端支持的授权类型.
     */
    @Schema(description = "客户端支持的授权类型")
    private Set<String> authorizedGrantType;

    /**
     * 重定向地址.
     */
    @Schema(description = "重定向地址")
    private Set<String> redirectUri;

    /**
     * Openid Connect登出后跳转地址.
     */
    @Schema(description = "Openid Connect登出后跳转地址")
    private Set<String> logoutRedirectUri;

    /**
     * 客户端申请的权限范围.
     */
    @Schema(description = "客户端申请的权限范围")
    private Set<String> scope;

    /**
     * 客户端设置
     */
    @Schema(title = "客户端设置")
    private ClientSettingDTO clientSetting;

    /**
     * 客户端申请的access token设置
     */
    @Schema(title = "客户端申请的access token设置")
    private TokenSettingDTO tokenSetting;
}
