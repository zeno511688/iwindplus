/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2026/05/17 18:19
 */
public final class LoadbalancerConstant {

    private LoadbalancerConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 负载均衡实例选择 Observation 名称.
     */
    public static final String SELECTION_OBSERVATION = "loadbalancer.selection";

    /**
     * 负载均衡实例数量指标名称.
     */
    public static final String INSTANCE_COUNT_METRIC = "loadbalancer.instances";

    /**
     * Observation 服务标签.
     */
    public static final String TAG_SERVICE = "service";

    /**
     * Observation 版本标签.
     */
    public static final String TAG_VERSION = "version";

    /**
     * Observation 路由标签.
     */
    public static final String TAG_ROUTE = "route";

    /**
     * Observation 结果标签.
     */
    public static final String TAG_OUTCOME = "outcome";

    /**
     * 成功结果.
     */
    public static final String OUTCOME_SUCCESS = "success";

    /**
     * 空结果.
     */
    public static final String OUTCOME_EMPTY = "empty";

    /**
     * 无目标版本标识.
     */
    public static final String VERSION_NONE = "none";

    /**
     * 请求头版本描述前缀.
     */
    public static final String HEADER_VERSION_DESC_PREFIX = "header ";

    /**
     * 百分比计算基数.
     */
    public static final int PERCENTAGE_BASE = NumberConstant.NUMBER_ONE_HUNDRED;

    /**
     * Nacos 实例默认权重.
     */
    public static final double DEFAULT_WEIGHT = 1.0D;

}
