/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.async.cmd.domain.constant.AsyncCmdConstant;
import com.iwindplus.base.domain.enums.BaseEnum;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 异步回调结果回调枚举定义.
 *
 * @author zengdegui
 * @since 2025/9/14
 */
@Getter
@RequiredArgsConstructor
public enum AsyncCmdCallbackResultEnum implements BaseEnum<Integer> {

    /**
     * 等待.
     */
    WAITING(20, "等待"),

    /**
     * 成功.
     */
    SUCCESS(30, "成功"),

    /**
     * 失败.
     */
    FAILED(40, "失败"),

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
     * 从result中解析预存的回调结果.
     *
     * @param result 结果集
     * @return AsyncCmdCallbackResultEnum，无预存时返回WAITING以外的null
     */
    public static AsyncCmdCallbackResultEnum fromResultMap(Map<String, Object> result) {
        if (Objects.isNull(result)) {
            return null;
        }

        final Object value = result.get(AsyncCmdConstant.CALLBACK_RESULT_KEY);
        if (Objects.isNull(value)) {
            return null;
        }

        try {
            return AsyncCmdCallbackResultEnum.valueOf(String.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
