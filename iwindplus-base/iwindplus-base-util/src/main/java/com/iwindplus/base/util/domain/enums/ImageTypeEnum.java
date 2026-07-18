/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 图片类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum ImageTypeEnum {
    /**
     * png.
     */
    PNG,

    /**
     * jpg.
     */
    JPG,

    /**
     * jpeg.
     */
    JPEG,

    /**
     * bmp.
     */
    BMP,

    /**
     * gif.
     */
    GIF,

    /**
     * svg.
     */
    SVG,
}