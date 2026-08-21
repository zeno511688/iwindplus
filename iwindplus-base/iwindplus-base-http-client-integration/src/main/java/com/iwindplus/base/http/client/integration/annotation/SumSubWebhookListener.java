/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.integration.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SumSub Webhook事件监听器注解.
 *
 * @author zengdegui
 * @since 2026/08/21
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SumSubWebhookListener {

    /**
     * 监听的Webhook事件类型.
     * <p>
     * 可以使用SumSubConstant中定义的常量，也可以自定义事件类型字符串。
     * </p>
     *
     * @return Webhook事件类型字符串
     */
    String value();
}
