/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */
package com.iwindplus.base.es.support;

/**
 * ES查询包装器静态工厂.
 *
 * @author zengdegui
 * @since 2026/08/22
 */
public final class EsWrappers {

    private EsWrappers() {
    }

    /**
     * 创建普通查询包装器.
     *
     * @return 普通查询包装器
     */
    public static EsQueryWrapper query() {
        return new EsQueryWrapper();
    }

    /**
     * 创建 Lambda 查询包装器.
     *
     * @param <T> 实体类型
     * @return Lambda 查询包装器
     */
    public static <T> EsLambdaQueryWrapper<T> lambdaQuery() {
        return new EsLambdaQueryWrapper<>();
    }

}
