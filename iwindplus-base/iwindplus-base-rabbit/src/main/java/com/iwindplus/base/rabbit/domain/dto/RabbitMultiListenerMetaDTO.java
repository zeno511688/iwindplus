/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.domain.dto;

import java.io.Serializable;
import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Rabbit 监听器元数据定义.
 *
 * @author zengdegui
 * @since 2026/03/26 00:59
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RabbitMultiListenerMetaDTO implements Serializable {

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
     * 监听器队列
     */
    private String[] queues;

    /**
     * 消费组
     */
    private String group;
}
