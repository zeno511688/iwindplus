/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * 长度工具类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class LengthUtil {

    private LengthUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 判断长度是否合理 （当只存在min时，判断最小值，当只存在max时，判断最大值，当min和max都存在时，判断是否在范围内）.
     *
     * @param length 长度
     * @param min    最小值
     * @param max    最大值
     * @return boolean
     */
    public static boolean checkLength(int length, Integer min, Integer max) {
        if (ObjectUtil.isEmpty(min) && ObjectUtil.isEmpty(max)) {
            throw new BizException(BizCodeEnum.MIN_MAX_EMPTY);
        }
        if (ObjectUtil.isNotEmpty(min) && ObjectUtil.isEmpty(max)) {
            return length >= min;
        } else if (ObjectUtil.isEmpty(min) && ObjectUtil.isNotEmpty(max)) {
            return length <= max;
        } else {
            return length >= min && length <= max;
        }
    }
}
