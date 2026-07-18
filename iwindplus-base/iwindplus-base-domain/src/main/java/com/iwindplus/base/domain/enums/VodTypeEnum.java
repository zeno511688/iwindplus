/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 视频点播类型枚举定义.
 *
 * @author zengdegui
 * @since 2024/11/17 11:16
 */

@Getter
@RequiredArgsConstructor
public enum VodTypeEnum implements BaseEnum<Integer> {
    /**
     * 阿里云.
     */
    ALIYUN(0, "阿里云"),

    /**
     * 七牛云.
     */
    QINIU(1, "七牛云"),

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
}
