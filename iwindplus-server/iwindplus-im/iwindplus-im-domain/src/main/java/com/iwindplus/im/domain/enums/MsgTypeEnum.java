/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.enums.BaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 消息类型枚举.
 *
 * @author zengdegui
 * @since 2021/7/8
 */
@Getter
@RequiredArgsConstructor
public enum MsgTypeEnum implements BaseEnum<Integer> {
    /**
     * 文本.
     */
    TEXT(0, "文本"),

    /**
     * 图片.
     */
    IMAGE(1, "图片"),

    /**
     * 语音.
     */
    VOICE(2, "语音"),

    /**
     * 视频.
     */
    VIDEO(3, "视频"),

    /**
     * 地理位置.
     */
    LOCATION(4, "地理位置"),

    /**
     * 链接.
     */
    LINK(5, "链接"),

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
