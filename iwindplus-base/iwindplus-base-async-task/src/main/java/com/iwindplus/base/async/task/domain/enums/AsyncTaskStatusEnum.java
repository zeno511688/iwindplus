/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.task.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 异步任务状态枚举定义.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Getter
@RequiredArgsConstructor
public enum AsyncTaskStatusEnum implements BaseEnum<Integer> {
    /**
     * 待执行.
     */
    PENDING(0, "待执行"),

    /**
     * 执行中.
     */
    EXECUTING(10, "执行中"),

    /**
     * 等待中.
     */
    WAITING(20, "等待中"),

    /**
     * 成功.
     */
    SUCCESS(30, "成功"),

    /**
     * 失败.
     */
    FAILED(40, "失败"),

    /**
     * 丢弃.
     */
    DISCARD(50, "丢弃"),

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
     * 获取未完成状态
     *
     * @return List<AsyncTaskStatusEnum>
     */
    public static List<AsyncTaskStatusEnum> getUnfinishedStatus() {
        return List.of(AsyncTaskStatusEnum.PENDING, AsyncTaskStatusEnum.EXECUTING, AsyncTaskStatusEnum.WAITING, AsyncTaskStatusEnum.FAILED);
    }
}
