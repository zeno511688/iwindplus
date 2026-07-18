/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * 无状态令牌类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ShiroTokenDTO implements AuthenticationToken {

    /**
     * 访问token.
     */
    private String accessToken;

    @Override
    public Object getPrincipal() {
        return this.accessToken;
    }

    @Override
    public Object getCredentials() {
        return this.accessToken;
    }
}