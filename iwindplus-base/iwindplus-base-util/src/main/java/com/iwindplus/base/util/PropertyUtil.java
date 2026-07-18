/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * Property工具类.
 *
 * @author zengdegui
 * @since 2026/01/02 20:34
 */
@Slf4j
public class PropertyUtil {

    private PropertyUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 将 JSON 字符串转换为 Properties.
     *
     * @param text JSON 字符串
     * @return Properties
     */
    public static Properties parseToProperties(String text) {
        Properties properties = new Properties();
        final Map<String, Object> jsonMap = JacksonUtil.parseMap(text);
        if (MapUtil.isEmpty(jsonMap)) {
            return properties;
        }

        for (Entry<String, Object> entry : jsonMap.entrySet()) {
            if (ObjectUtil.isEmpty(entry.getValue())) {
                continue;
            }
            properties.put(entry.getKey(), entry.getValue().toString());
        }
        return properties;
    }
}
