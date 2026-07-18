/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.support;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.iwindplus.base.domain.annotation.Sensitive;
import java.util.Objects;
import lombok.AllArgsConstructor;

/**
 * 数据脱敏注解内置器.
 *
 * @author zengdegui
 * @since 2024/11/27 23:48
 */
@AllArgsConstructor
public class SensitiveAnnotationIntrospect extends NopAnnotationIntrospector {

    private final Boolean enabled;

    @Override
    public Object findSerializer(Annotated ann) {
        Sensitive annotation = ann.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation)) {
            return new SensitiveSerializer(this.enabled, annotation);
        }
        return null;
    }
}
