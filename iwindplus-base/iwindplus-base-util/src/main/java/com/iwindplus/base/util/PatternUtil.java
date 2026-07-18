/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.util.regex.Pattern;

/**
 * Pattern 工具类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public class PatternUtil extends ReUtil {

    private PatternUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 模糊匹配表格式.
     *
     * @param param 参数
     * @return Pattern
     */
    public static Pattern getPatternLike(String param) {
        if (param == null) {
            return null;
        }

        final String regex = String.format("%s%s%s", "^.*", param.trim(), ".*$");
        return PatternPool.get(regex, Pattern.CASE_INSENSITIVE);
    }

}
