/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support.serializer;

import cn.hutool.core.util.PrimitiveArrayUtil;
import com.iwindplus.base.util.KryoUtil;
import com.iwindplus.base.util.domain.dto.DataTransDTO;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Kryo 序列化.
 *
 * @author zengdegui
 * @since 2024/01/21
 */
@Slf4j
public class KryoRedisSerializer implements RedisSerializer<Object> {

    @Override
    public byte[] serialize(Object t) throws SerializationException {
        if (Objects.isNull(t)) {
            return new byte[0];
        }

        try {
            return KryoUtil.toJsonBytes(new DataTransDTO(t));
        } catch (Exception ex) {
            throw new SerializationException("Kryo Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (PrimitiveArrayUtil.isEmpty(bytes)) {
            return null;
        }

        try {
            final DataTransDTO dto = KryoUtil.parseBytes(bytes, DataTransDTO.class);
            return Optional.ofNullable(dto).map(DataTransDTO::getData).orElse(null);
        } catch (Exception ex) {
            throw new SerializationException("Kryo Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
