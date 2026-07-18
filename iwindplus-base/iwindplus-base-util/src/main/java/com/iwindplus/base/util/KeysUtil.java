/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.KeyUtil;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import java.lang.reflect.Method;

/**
 * 唯一key工具类.
 *
 * @author zengdegui
 * @since 2024/07/06 12:39
 */
public class KeysUtil extends KeyUtil {

    /**
     * 拼接生成key.
     *
     * @param enabledCrypto 是否启用加密处理
     * @param target        类
     * @param method        方法
     * @param params        参数
     * @return String
     */
    public static String generate(
        Boolean enabledCrypto,
        Object target,
        Method method,
        Object... params) {

        String baseKey = target.getClass().getName()
            + SymbolConstant.COLON
            + method.getName();

        String paramKey = serializeParams(params);

        String rawKey = CharSequenceUtil.isBlank(paramKey)
            ? baseKey
            : baseKey + SymbolConstant.WELL_NO + paramKey;

        if (Boolean.TRUE.equals(enabledCrypto)) {
            return CryptoUtil.encryptBySm3(rawKey);
        }

        return rawKey;
    }

    private static String serializeParams(Object... params) {
        if (params == null || params.length == 0) {
            return null;
        }

        return JacksonUtil.toJsonStr(params);
    }
}