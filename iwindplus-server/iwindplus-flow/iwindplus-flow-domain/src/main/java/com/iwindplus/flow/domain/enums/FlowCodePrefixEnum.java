/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.flow.domain.enums;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Objects;

/**
 * 编码前缀枚举.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@Accessors(fluent = true)
public enum FlowCodePrefixEnum {
    /**
     * 流程分类前缀.
     */
    CATEGORY_PREFIX("category_", "流程分类前缀"),

    /**
     * 流程表单前缀.
     */
    FORM_PREFIX("form_", "流程表单前缀"),

    /**
     * 流程模型前缀.
     */
    MODEL_PREFIX("model_", "流程模型前缀"),

    ;

    /**
     * 编码.
     */
    private final String code;

    /**
     * 描述.
     */
    private final String desc;

    FlowCodePrefixEnum(final String code, final String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 通过编码查找枚举.
     *
     * @param code 编码
     * @return BpmCodePrefixEnum
     */
    public static FlowCodePrefixEnum valueOfCode(String code) {
        return Arrays.stream(FlowCodePrefixEnum.values())
                .filter(m -> Objects.equals(code, m.code())).findFirst().orElse(null);
    }
}
