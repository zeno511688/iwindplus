/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.DesensitizedUtil.DesensitizedType;
import cn.hutool.core.util.ObjectUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.SensitiveTypeEnum;
import com.iwindplus.base.util.domain.dto.SensitiveDTO;
import java.util.EnumMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏相关工具类.
 *
 * @author zengdegui
 * @since 2025/04/19 00:40
 */
@Slf4j
public class SensitiveUtil {

    private SensitiveUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    private static final Map<SensitiveTypeEnum, DesensitizedType> TYPE_MAPPING;

    static {
        TYPE_MAPPING = new EnumMap<>(SensitiveTypeEnum.class);
        for (SensitiveTypeEnum s : SensitiveTypeEnum.values()) {
            try {
                TYPE_MAPPING.put(s, DesensitizedType.valueOf(s.name()));
            } catch (IllegalArgumentException ex) {
                // 没有对应 Hutool 规则就留空，走原始值
                log.error("【脱敏】没有对应 规则，type={}", s, ex);
            }
        }
    }

    /**
     * 脱敏.
     *
     * @param data   待脱敏数据
     * @param config 脱敏配置
     * @return String
     */
    public static String desensitized(String data, SensitiveDTO config) {
        if (CharSequenceUtil.isBlank(data) || config == null || ObjectUtil.isEmpty(config.getType())) {
            return data;
        }
        try {
            return doDesensitized(data, config);
        } catch (Exception e) {
            log.warn("【脱敏】异常 data={} config={}", data, config, e);
        }
        return data;
    }

    private static String doDesensitized(String data, SensitiveDTO config) {
        if (config.getType() == SensitiveTypeEnum.CUSTOM) {
            int start = config.getStartInclude();
            int end = data.length() - config.getEndReserve();
            // 早返回：下标非法直接原样返回
            if (start < 0 || end < 0 || start > end) {
                log.warn("【脱敏】自定义下标非法 start={} end={} data={}", start, end, data);
                return data;
            }
            return CharSequenceUtil.hide(data, start, end);
        }

        DesensitizedType dt = TYPE_MAPPING.get(config.getType());
        return dt == null ? data : DesensitizedUtil.desensitized(data, dt);
    }

}
