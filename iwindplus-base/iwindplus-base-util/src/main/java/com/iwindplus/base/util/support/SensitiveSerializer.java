/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.support;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.iwindplus.base.domain.annotation.Sensitive;
import com.iwindplus.base.util.SensitiveUtil;
import com.iwindplus.base.util.domain.dto.SensitiveDTO;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏序列化.
 *
 * @author zengdegui
 * @since 2024/11/27 00:09
 */
@Slf4j
public class SensitiveSerializer extends JsonSerializer<String> {

    /**
     * 脱敏是否开启（全局）.
     */
    private Boolean enabled;

    /**
     * 脱敏对象
     */
    private Sensitive sensitive;

    /**
     * 构造函数.
     *
     * @param enabled   脱敏是否开启（全局）
     * @param sensitive 脱敏对象
     */
    public SensitiveSerializer(Boolean enabled, Sensitive sensitive) {
        this.enabled = enabled;
        this.sensitive = sensitive;
    }

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String result = this.getDesensitizedData(value);
        if (ObjectUtil.isEmpty(result)) {
            jsonGenerator.writeNull();
            return;
        }
        jsonGenerator.writeString(result);
    }

    private String getDesensitizedData(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }

        if (Boolean.FALSE.equals(this.enabled) || Objects.isNull(this.sensitive) || Boolean.FALSE.equals(this.sensitive.enabled())) {
            return value;
        }

        SensitiveDTO config = SensitiveDTO.builder()
            .type(this.sensitive.type())
            .startInclude(this.sensitive.startInclude())
            .endReserve(this.sensitive.endReserve())
            .build();
        return SensitiveUtil.desensitized(value, config);
    }
}
