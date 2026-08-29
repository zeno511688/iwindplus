/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 导出任务状态枚举.
 *
 * @author zengdegui
 * @since 2026/08/27
 */
@Getter
@RequiredArgsConstructor
public enum ExportTaskStatusEnum implements BaseEnum<Integer> {
    /**
     * 待执行.
     */
    PENDING(0, "待执行"),

    /**
     * 执行中.
     */
    EXECUTING(10, "执行中"),

    /**
     * 成功.
     */
    SUCCESS(20, "成功"),

    /**
     * 失败.
     */
    FAILED(30, "失败"),

    /**
     * 丢弃.
     */
    DISCARD(40, "丢弃"),
    ;

    /**
     * 值.
     */
    @EnumValue
    private final Integer value;

    /**
     * 描述.
     */
    private final String desc;

    /**
     * 获取未完成状态列表.
     *
     * @return 未完成状态列表
     */
    public static List<ExportTaskStatusEnum> getUnfinishedStatus() {
        return Arrays.asList(PENDING, EXECUTING);
    }
}
