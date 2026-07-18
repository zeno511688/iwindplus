/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class FlowConstant {
    private FlowConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 流程服务名.
     */
    public static final String FLOW_SERVER_NAME = "iwindplus-flow";

    /**
     * 流程服务客户端扫描包名.
     */
    public static final String FLOW_CLIENT_SCAN_BASE_PACKAGE = "com.iwindplus.flow.client";

    /**
     * redis 缓存相关常数.
     */
    public static class RedisCacheConstant {

        private RedisCacheConstant() {
            throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
        }

        /**
         * 流程分类缓存名称.
         */
        public static final String CACHE_FLOW_CATEGORY = "flowCategory";

        /**
         * 流程表单缓存名称.
         */
        public static final String CACHE_FLOW_FORM= "flowForm";

        /**
         * 模型缓存名称.
         */
        public static final String CACHE_FLOW_MODEL = "flowModel";

    }
}
