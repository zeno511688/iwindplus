/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.rabbit.domain.enums;

import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交换机类型枚举.
 *
 * @author zengdegui
 * @since 2020/6/13
 */
@Getter
@RequiredArgsConstructor
public enum RabbitExchangeTypeEnum implements BaseEnum<String> {
    /**
     * 直连交换机. 根据routing-key精准匹配队列(最常使用).
     */
    DIRECT("direct", "直连交换机"),

    /**
     * 主题交换机. 根据routing-key模糊匹配队列，*匹配任意一个字符，#匹配0个或多个字符.
     */
    TOPIC("topic", "主题交换机"),

    /**
     * 扇形交换机. 直接分发给所有绑定的队列，忽略routing-key,用于广播消息.
     */
    FANOUT("fanout", "扇形交换机"),

    /**
     * 头交换机. 类似直连交换机，不同于直连交换机的路由规则建立在头属性上而不是routing-key(使用较少)
     */
    HEADERS("headers", "头交换机"),

    /**
     * 延时交换机. 实现延时消费.
     */
    DELAYED("delayed", "延时交换机");

    /**
     * 值.
     */
    private final String value;

    /**
     * 描述.
     */
    private final String desc;

}
