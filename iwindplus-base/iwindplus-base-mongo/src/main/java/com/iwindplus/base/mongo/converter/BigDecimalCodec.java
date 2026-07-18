/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mongo.converter;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.Decimal128;

import java.math.BigDecimal;

/**
 * BigDecimal存储为MongoDB中的字符串转换器.
 *
 * @author zengdegui
 * @since 2024/09/10 21:55
 */
public class BigDecimalCodec implements Codec<BigDecimal> {
    @Override
    public void encode(BsonWriter writer, BigDecimal value, EncoderContext encoderContext) {
        writer.writeDecimal128(new Decimal128(value));
    }

    @Override
    public BigDecimal decode(BsonReader reader, DecoderContext decoderContext) {
        return reader.readDecimal128().bigDecimalValue();
    }

    @Override
    public Class<BigDecimal> getEncoderClass() {
        return BigDecimal.class;
    }
}
