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
 * db操作类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum DbActionTypeEnum implements BaseEnum<Integer> {

    /**
     * 插入.
     */
    INSERT(0, "c", "插入"),

    /**
     * 删除.
     */
    DELETE(1, "d", "删除"),

    /**
     * 修改.
     */
    UPDATE(2, "u", "修改"),

    /**
     * 查询.
     */
    SELECT(3, "r", "查询"),

    /**
     * 未知.
     */
    UNKNOWN(4, "unknown", "未知");

    /**
     * 值.
     */
    @EnumValue
    private final Integer value;

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
     * @return DbOperateTypeEnum
     */
    public static DbActionTypeEnum fromAlias(String alias) {
        return Arrays.stream(values())
            .filter(e -> Objects.equals(e.getAlias(), alias))
            .findFirst()
            .orElse(null);
    }
}