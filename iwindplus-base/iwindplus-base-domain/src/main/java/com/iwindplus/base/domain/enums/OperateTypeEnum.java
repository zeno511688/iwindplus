/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 操作类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum OperateTypeEnum implements BaseEnum<String> {

    /**
     * 添加.
     */
    ADD("add", "insert", "添加"),

    /**
     * 删除.
     */
    DELETE("delete", "delete", "删除"),

    /**
     * 修改.
     */
    MODIFY("modify", "update", "修改"),

    /**
     * 查询.
     */
    QUERY("query", "select", "查询"),

    /**
     * 未知.
     */
    UNKNOWN("unknown", "unknown", "未知");

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 别名.
     */
    private final String alias;

    /**
     * 描述.
     */
    private final String desc;

    /**
     * 通过别名获取对应的枚举.
     *
     * @param alias 别名
     * @return OperateTypeEnum
     */
    public static OperateTypeEnum fromAlias(String alias) {
        return Arrays.stream(values())
            .filter(e -> Objects.equals(e.getAlias(), alias))
            .findFirst()
            .orElse(null);
    }
}