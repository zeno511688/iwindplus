/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.gateway.infrastructure.configuration.property;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 操作扩展相关属性.
 *
 * @author zengdegui
 * @since 2026/09/14 20:21
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@RefreshScope
@ConfigurationProperties(prefix = "gateway.operate-extend")
public class OperateExtendProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.FALSE;

    /**
     * 忽略的账号.
     */
    private List<String> ignoredUser;

    /**
     * 忽略的API.
     */
    @Builder.Default
    private List<String> ignoredApi = List.of(
        "admin/mgt/user/getGaQrcode",
        "admin/mgt/user/editGaBindFlag",
        "admin/mgt/user/userExtendYubikey/save"
    );

    /**
     * 需要GA校验的API.
     */
    private List<String> includeGaApi;

    /**
     * 需要邮箱校验的API.
     */
    private List<String> includeMailApi;

    /**
     * 需要短信校验的API.
     */
    private List<String> includeSmsApi;

    /**
     * 需要yubikey校验的API.
     */
    private List<String> includeYubikeyApi;
}
