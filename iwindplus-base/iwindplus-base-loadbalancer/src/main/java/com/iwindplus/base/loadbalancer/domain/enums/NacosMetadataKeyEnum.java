/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Nacos元数据键枚举.
 *
 * @author zengdegui
 * @since 2024/12/28
 */
@Getter
@RequiredArgsConstructor
public enum NacosMetadataKeyEnum implements BaseEnum<String> {

    /**
     * Nacos集群.
     */
    CLUSTER("nacos.cluster", "Nacos集群"),

    /**
     * Nacos权重.
     */
    WEIGHT("nacos.weight", "Nacos权重"),

    /**
     * Nacos健康状态.
     */
    HEALTHY("nacos.healthy", "Nacos健康状态"),
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
