/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.support.serializer;

import cn.hutool.core.util.PrimitiveArrayUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.FileCopyUtils;

/**
 * gzip压缩序列化数据.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2024/01/21 18:38
 */
@Slf4j
public class GzipRedisSerializer<T> implements RedisSerializer<T> {

    private RedisSerializer<T> redisSerializer;

    public GzipRedisSerializer(RedisSerializer<T> redisSerializer) {
        this.redisSerializer = redisSerializer;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (Objects.isNull(t)) {
            return new byte[0];
        }

        byte[] bytes = this.redisSerializer.serialize(t);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException("Gzip Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (PrimitiveArrayUtil.isEmpty(bytes)) {
            return null;
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            FileCopyUtils.copy(inputStream, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return this.redisSerializer.deserialize(byteArrayOutputStream.toByteArray());
        } catch (IOException ex) {
            throw new SerializationException("Gzip Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
