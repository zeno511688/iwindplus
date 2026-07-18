/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.iwindplus.base.domain.constant.CommonConstant.PageConstant;
import java.io.IOException;

/**
 * 分页序列化.
 *
 * @author zengdegui
 * @since 2025/10/18 11:38
 */
public class PageSerializer extends JsonSerializer<Page> {

    /**
     * 全局是否开启.
     */
    private final boolean globalEnabled;

    public PageSerializer(boolean globalEnabled) {
        this.globalEnabled = globalEnabled;
    }

    @Override
    public void serialize(Page value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        if (Boolean.FALSE.equals(this.globalEnabled)) {
            serializerProvider.defaultSerializeValue(value, gen);
            return;
        }

        gen.writeStartObject();
        gen.writeObjectField(PageConstant.RECORDS, value.getRecords());
        gen.writeNumberField(PageConstant.TOTAL, value.getTotal());
        gen.writeNumberField(PageConstant.PAGES, value.getPages());
        gen.writeNumberField(PageConstant.CURRENT, value.getCurrent());
        gen.writeNumberField(PageConstant.SIZE, value.getSize());
        gen.writeEndObject();
    }
}
