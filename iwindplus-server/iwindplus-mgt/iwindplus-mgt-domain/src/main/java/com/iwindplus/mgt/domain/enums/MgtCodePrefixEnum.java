/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 编码前缀枚举.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum MgtCodePrefixEnum implements BaseEnum<String> {
    /**
     * API白名单前缀.
     */
    API_WHITE_LIST_PREFIX("api_white_list_", "API白名单前缀"),

    /**
     * 系统前缀.
     */
    SYSTEM_PREFIX("system_", "系统前缀"),

    /**
     * 组织前缀.
     */
    ORG_PREFIX("org_", "组织前缀"),

    /**
     * 部门前缀.
     */
    DEPARTMENT_PREFIX("department_", "部门前缀"),

    /**
     * 职位前缀.
     */
    POSITION_PREFIX("position_", "职位前缀"),

    /**
     * 用户前缀.
     */
    USER_PREFIX("user_", "用户前缀"),

    /**
     * 角色前缀.
     */
    ROLE_PREFIX("role_", "角色前缀"),

    /**
     * 菜单前缀.
     */
    MENU_PREFIX("menu_", "菜单前缀"),

    /**
     * 用户组前缀.
     */
    USER_GROUP_PREFIX("userGroup_", "用户组前缀"),

    /**
     * 国际化项目前缀.
     */
    I18N_PROJECT_PREFIX("i18n_project_", "国际化项目前缀"),
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
