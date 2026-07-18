/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.PrimitiveArrayUtil;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.util.domain.dto.DataTransDTO;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * Kryo工具类.
 *
 * <p>
 * 使用 ThreadLocal 方式管理 Kryo 实例，
 * 避免 Kryo 非线程安全问题，同时移除对象池复杂度。
 * </p>
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class KryoUtil {

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private KryoUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * ThreadLocal Kryo 实例.
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL =
        ThreadLocal.withInitial(KryoUtil::createDefaultObject);

    /**
     * 通用的 Kryo 执行方法，执行指定操作。
     *
     * @param action 执行的操作
     * @param <T>    返回的类型
     * @return 执行结果
     */
    public static <T> T executeAction(KryoAction<T> action) {
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        try {
            return action.execute(kryo);
        } catch (Exception ex) {
            log.error("Kryo action execution error", ex);
            throw new BizException(BizCodeEnum.EXECUTE_ERROR);
        } finally {
            // 防止状态污染
            kryo.reset();
        }
    }

    /**
     * 序列化为字节数组.
     *
     * @param obj 任意对象
     * @param <T> 对象的类型
     * @return byte[]
     */
    public static <T> byte[] toJsonBytes(T obj) {
        if (obj == null) {
            return new byte[0];
        }

        return executeAction(kryo -> {
            try (Output output = new Output(DEFAULT_BUFFER_SIZE, -1)) {
                kryo.writeClassAndObject(output, obj);
                return output.toBytes();
            }
        });
    }

    /**
     * 反序列化为对象.
     *
     * @param bytes 字节数组
     * @param clazz 目标类
     * @param <T>   泛型
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseBytes(byte[] bytes, Class<T> clazz) {
        if (PrimitiveArrayUtil.isEmpty(bytes)) {
            return null;
        }

        return executeAction(kryo -> {
            try (Input input = new Input(bytes)) {
                Object obj = kryo.readClassAndObject(input);
                return clazz.cast(obj);
            }
        });
    }

    /**
     * 创建默认 Kryo 对象.
     *
     * @return Kryo
     */
    public static Kryo createDefaultObject() {
        Kryo kryo = new Kryo();

        // 无循环引用时关闭
        kryo.setReferences(true);

        // 是否强制注册
        kryo.setRegistrationRequired(false);

        // Kryo 5.5+ 泛型优化
        kryo.setOptimizedGenerics(true);

        // 自动 reset
        kryo.setAutoReset(true);

        // 实例化策略：反射 → Unsafe → 无参构造 兜底
        DefaultInstantiatorStrategy strategy =
            new DefaultInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setInstantiatorStrategy(strategy);

        // 常用对象提前注册
        kryo.register(DataTransDTO.class, 100);
        kryo.register(Tree.class, 101);

        log.info("Kryo ThreadLocal instance created");
        return kryo;
    }

    @FunctionalInterface
    private interface KryoAction<T> {

        /**
         * 执行.
         *
         * @param entity Kryo 对象
         * @return T
         * @throws Exception
         */
        T execute(Kryo entity) throws Exception;
    }
}
