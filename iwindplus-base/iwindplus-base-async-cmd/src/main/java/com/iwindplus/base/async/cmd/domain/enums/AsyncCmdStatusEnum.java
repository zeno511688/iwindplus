/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 异步命令状态枚举定义.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Getter
@RequiredArgsConstructor
public enum AsyncCmdStatusEnum implements BaseEnum<Integer> {
    /**
     * 待执行.
     */
    TO_BE_EXECUTE(0, "待执行"),

    /**
     * 执行中.
     */
    EXECUTE(10, "执行中"),

    /**
     * 异步等待.
     */
    ASYNC_WAIT(20, "异步等待"),

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
     * 获取需要重置的
     *
     * @return List<AsyncCmdStatusEnum>
     */
    public static List<AsyncCmdStatusEnum> getPendingStatus() {
        return List.of(AsyncCmdStatusEnum.EXECUTE, AsyncCmdStatusEnum.FAILED);
    }

    /**
     * 获取未完成状态
     *
     * @return List<AsyncCmdStatusEnum>
     */
    public static List<AsyncCmdStatusEnum> getUnfinishedStatus() {
        return List.of(AsyncCmdStatusEnum.TO_BE_EXECUTE, AsyncCmdStatusEnum.EXECUTE, AsyncCmdStatusEnum.ASYNC_WAIT, AsyncCmdStatusEnum.FAILED);
    }
}
