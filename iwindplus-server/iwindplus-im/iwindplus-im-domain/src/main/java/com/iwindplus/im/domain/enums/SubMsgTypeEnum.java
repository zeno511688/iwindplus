/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.im.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 子消息类型枚举.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Getter
@RequiredArgsConstructor
public enum SubMsgTypeEnum implements BaseEnum<String> {

    /**
     * 访问token过期.
     */
    ACCESS_TOKEN_EXPIRED("access_token_expired", "访问token过期"),

    /**
     * 刷新角色权限.
     */
    REFRESH_ROLE_PERMISSION("refresh_role_permission", "刷新角色权限"),

    /**
     * 刷新按钮权限.
     */
    REFRESH_BUTTON_PERMISSION("refresh_button_permission", "刷新按钮权限"),

    ;

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 描述.
     */
    private final String desc;
}
