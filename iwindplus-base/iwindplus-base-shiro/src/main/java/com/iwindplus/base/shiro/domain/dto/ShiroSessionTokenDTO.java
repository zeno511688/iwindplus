/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.shiro.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 用户和密码（包含验证码）令牌类数据传输对象（有状态）.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShiroSessionTokenDTO extends UsernamePasswordToken {

    /**
     * 构造方法.
     *
     * @param username   用户名（必填）
     * @param password   密码 必填）
     * @param rememberMe 是否记住我（为null默认false,否则true）
     */
    public ShiroSessionTokenDTO(final String username, final String password, final Boolean rememberMe) {
        super(username, password, Boolean.TRUE.equals(rememberMe));
    }
}
