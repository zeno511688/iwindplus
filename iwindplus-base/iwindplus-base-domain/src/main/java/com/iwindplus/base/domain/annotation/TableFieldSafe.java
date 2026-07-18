/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.annotation;

import com.iwindplus.base.domain.enums.AlgorithmTypeEnum;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表字段安全处理注解.（加密/解密）
 *
 * @author zengdegui
 * @since 2024/11/26 23:53
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TableFieldSafe {

    /**
     * 是否启用输入加密（可选，默认：true）.
     *
     * @return boolean
     */
    boolean inputEncrypt() default true;

    /**
     * 是否启用输出解密（可选，默认：true）.
     *
     * @return boolean
     */
    boolean outputDecrypt() default true;

    /**
     * 算法（可选，默认：AES）（注解指定的算法优先）.
     * </p>
     * {@link AlgorithmTypeEnum}
     */
    AlgorithmTypeEnum algorithm() default AlgorithmTypeEnum.AES;
}
