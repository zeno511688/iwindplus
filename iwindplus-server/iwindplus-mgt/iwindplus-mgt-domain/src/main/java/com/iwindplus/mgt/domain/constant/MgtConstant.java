/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class MgtConstant {

    private MgtConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 管理服务名.
     */
    public static final String MGT_SERVER_NAME = "iwindplus-mgt";

    /**
     * 管理服务客户端扫描包名.
     */
    public static final String MGT_CLIENT_SCAN_BASE_PACKAGE = "com.iwindplus.mgt.client";

    /**
     * 初始化备注.
     */
    public static final String REMARK_INIT = "init data";

    /**
     * redis 缓存相关常数.
     */
    public static class RedisCacheConstant {

        private RedisCacheConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 部门缓存名称.
         */
        public static final String CACHE_DEPARTMENT = "department";

        /**
         * 菜单缓存名称.
         */
        public static final String CACHE_MENU = "menu";

        /**
         * 组织缓存名称.
         */
        public static final String CACHE_ORG = "org";

        /**
         * 组织审核缓存名称.
         */
        public static final String CACHE_ORG_AUDIT = "orgAudit";

        /**
         * 组织扩展缓存名称.
         */
        public static final String CACHE_ORG_EXTEND = "orgExtend";

        /**
         * 用户组缓存名称.
         */
        public static final String CACHE_USER_GROUP = "userGroup";

        /**
         * 用户组用户关系缓存名称.
         */
        public static final String CACHE_USER_GROUP_USER = "userGroupUser";

        /**
         * 用户组角色关系缓存名称.
         */
        public static final String CACHE_USER_GROUP_ROLE = "userGroupRole";

        /**
         * 用户组织关系缓存名称.
         */
        public static final String CACHE_USER_ORG = "userOrg";

        /**
         * 用户职位关系缓存名称.
         */
        public static final String CACHE_USER_POSITION = "userPosition";

        /**
         * 用户部门关系缓存名称.
         */
        public static final String CACHE_USER_DEPARTMENT = "userDepartment";

        /**
         * 用户角色关系缓存名称.
         */
        public static final String CACHE_USER_ROLE = "userRole";

        /**
         * 用户扩展yubikey缓存名称.
         */
        public static final String CACHE_USER_EXTEND_YUBIKEY = "userYubikey";

        /**
         * 职位缓存名称.
         */
        public static final String CACHE_POSITION = "position";

        /**
         * 资源缓存名称.
         */
        public static final String CACHE_RESOURCE = "resource";

        /**
         * 角色缓存名称.
         */
        public static final String CACHE_ROLE = "role";

        /**
         * 用户缓存名称.
         */
        public static final String CACHE_USER = "user";

        /**
         * 服务缓存名称.
         */
        public static final String CACHE_SERVER = "server";

        /**
         * 角色菜单关系缓存名称.
         */
        public static final String CACHE_ROLE_MENU = "roleMenu";

        /**
         * 角色资源关系缓存名称.
         */
        public static final String CACHE_ROLE_RESOURCE = "roleResource";

        /**
         * 系统缓存名称.
         */
        public static final String CACHE_SYSTEM = "system";

        /**
         * API白名单缓存名称.
         */
        public static final String CACHE_API_WHITE_LIST = "apiWhiteList";

        /**
         * 应用缓存名称.
         */
        public static final String CACHE_APP = "app";

        /**
         * 应用凭证缓存名称.
         */
        public static final String CACHE_APP_CERT = "appCert";

        /**
         * 客户端缓存名称.
         */
        public static final String CACHE_CLIENT = "client";

        /**
         * 国际化项目缓存名称.
         */
        public static final String CACHE_I18N_PROJECT = "i18nProject";

        /**
         * 国际化消息缓存名称.
         */
        public static final String CACHE_I18N_MSG = "i18nMsg";

        /**
         * IP黑名单缓存名称.
         */
        public static final String CACHE_IP_BLACK_LIST = "ipBlackList";

        /**
         * 服务API缓存名称.
         */
        public static final String CACHE_SERVER_API = "serverApi";

        /**
         * 第三方绑定授权缓存名称.
         */
        public static final String CACHE_THIRD_BIND_GRANT = "thirdBindGrant";

    }
}
