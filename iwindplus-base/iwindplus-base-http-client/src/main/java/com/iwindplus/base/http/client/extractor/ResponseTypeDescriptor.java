/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.extractor;

import com.fasterxml.jackson.core.type.TypeReference;
import java.lang.reflect.Type;

/**
 * 响应类型描述符.
 *
 * @author zengdegui
 * @since 2026/01/21 00:44
 */

public record ResponseTypeDescriptor(Class<?> rawClass, Type genericType,
                                     TypeReference<?> typeReference) {

    /**
     * 创建一个ResponseTypeDescriptor实例.
     *
     * @param clazz 类
     * @return ResponseTypeDescriptor
     */
    public static ResponseTypeDescriptor forClass(Class<?> clazz) {
        return new ResponseTypeDescriptor(clazz, clazz, null);
    }

    /**
     * 创建一个ResponseTypeDescriptor实例.
     *
     * @param typeRef 类型引用
     * @return ResponseTypeDescriptor
     */
    public static ResponseTypeDescriptor forTypeReference(TypeReference<?> typeRef) {
        return new ResponseTypeDescriptor(null, typeRef.getType(), typeRef);
    }

    /**
     * 判断是否为类.
     *
     * @return true/false
     */
    public boolean isClass() {
        return rawClass != null;
    }

    /**
     * 判断是否为类型引用.
     *
     * @return true/false
     */
    public boolean isTypeReference() {
        return typeReference != null;
    }
}
