/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 模块名称枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum AuthModuleEnum implements BaseEnum<String> {
    /**
     * 登录.
     */
    LOGIN("login", "登录"),

    /**
     * 刷新token
     */
    REFRESH_TOKEN("refresh_token", "刷新token"),

    /**
     * 退出.
     */
    LOGOUT("logout", "退出"),
    ;

    /**
     * 值.
     */
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}
