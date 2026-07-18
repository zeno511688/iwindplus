/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support.serializer;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis 统一前缀.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PrefixRedisSerializer extends StringRedisSerializer {

    /**
     * 前缀.
     */
    private String keyPrefix;

    @Override
    public byte[] serialize(String string) {
        if (CharSequenceUtil.isBlank(string)) {
            return new byte[0];
        }

        String key;
        if (CharSequenceUtil.isNotBlank(this.keyPrefix)) {
            key = this.keyPrefix.concat(string);
        } else {
            key = string;
        }
        return super.serialize(key);
    }

    @Override
    public String deserialize(byte[] bytes) {
        if (PrimitiveArrayUtil.isEmpty(bytes)) {
            return null;
        }

        String result = new String(bytes, StandardCharsets.UTF_8);
        if (CharSequenceUtil.isNotBlank(result) && CharSequenceUtil.isNotBlank(this.keyPrefix)) {
            int index = result.indexOf(this.keyPrefix);
            if (index != -1) {
                return result.substring(index + this.keyPrefix.length());
            }
        }
        return result;
    }
}
