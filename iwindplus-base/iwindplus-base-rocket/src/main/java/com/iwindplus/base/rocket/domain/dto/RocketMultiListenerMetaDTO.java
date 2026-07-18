/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rocket.domain.dto;

import java.io.Serializable;
import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Rocket 监听器元数据定义.
 *
 * @author zengdegui
 * @since 2026/03/26 00:59
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RocketMultiListenerMetaDTO implements Serializable {

    /**
     * 监听器bean
     */
    private Object bean;

    /**
     * 监听器方法
     */
    private Method method;

    /**
     * 监听器集群
     */
    private String cluster;

    /**
     * 主题名称.
     */
    private String topic;

    /**
     * Tag（支持 * 或 多个用 || 分隔）
     */
    private String tag;

    /**
     * 消费组
     */
    private String group;

    /**
     * 是否顺序消费.
     */
    private Boolean orderly;
}
