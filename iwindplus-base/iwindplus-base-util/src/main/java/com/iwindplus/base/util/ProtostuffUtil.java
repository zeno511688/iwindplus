/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.util.PrimitiveArrayUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Protostuff工具类.
 *
 * @author zengdegui
 * @since 2024
 */
@Slf4j
public class ProtostuffUtil {

    private static final int DEFAULT_BUFFER_SIZE = 512;

    private ProtostuffUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * Schema LRU 缓存
     */
    private static final Map<Class<?>, Schema<?>> SCHEMA_CACHE =
        Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Class<?>, Schema<?>> eldest) {
                return size() > 256;
            }
        });

    /**
     * Protostuff 执行模板（统一异常处理）
     */
    public static <T> T executeAction(ProtostuffAction<T> action) {
        LinkedBuffer buffer = LinkedBuffer.allocate(DEFAULT_BUFFER_SIZE);
        try {
            return action.execute(buffer);
        } catch (Exception ex) {
            log.error("Protostuff action execution error", ex);
            throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 序列化
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] toJsonBytes(T obj) {
        if (obj == null) {
            return new byte[0];
        }

        return executeAction(buffer ->
            ProtostuffIOUtil.toByteArray(
                obj,
                getSchema((Class<T>) obj.getClass()),
                buffer
            )
        );
    }

    /**
     * 反序列化
     */
    public static <T> T parseBytes(byte[] bytes, Class<T> clazz) {
        if (PrimitiveArrayUtil.isEmpty(bytes)) {
            return null;
        }

        try {
            Schema<T> schema = getSchema(clazz);
            T instance = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(bytes, instance, schema);
            return instance;
        } catch (Exception ex) {
            log.error("Protostuff deserialize error, clazz={}", clazz.getName(), ex);
            throw new BizException(BizCodeEnum.DESERIALIZE_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        return (Schema<T>) SCHEMA_CACHE.computeIfAbsent(clazz, RuntimeSchema::createFrom);
    }

    @FunctionalInterface
    private interface ProtostuffAction<T> {

        /**
         * 执行.
         *
         * @param entity 对象
         * @return T
         * @throws Exception
         */
        T execute(LinkedBuffer entity) throws Exception;
    }
}
