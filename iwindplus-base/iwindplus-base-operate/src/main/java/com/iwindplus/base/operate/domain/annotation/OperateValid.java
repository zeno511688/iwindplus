/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.operate.domain.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作校验注解（参数通过请求头传递，不能同时支持多种校验）.
 *
 * @author zengdegui
 * @since 2024/11/26 23:53
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateValid {

    /**
     * 是否开启GA验证码校验（可选，默认：false）.
     *
     * @return boolean
     */
    boolean enabledGa() default false;

    /**
     * 是否开启邮箱验证码校验（可选，默认：false）.
     *
     * @return boolean
     */
    boolean enabledMail() default false;

    /**
     * 是否开启短信验证码校验（可选，默认：false）.
     *
     * @return boolean
     */
    boolean enabledSms() default false;

    /**
     * 是否开启yubikey校验（可选，默认：false）.
     *
     * @return boolean
     */
    boolean enabledYubikey() default false;
}
