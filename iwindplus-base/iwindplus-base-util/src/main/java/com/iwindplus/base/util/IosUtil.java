/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.io.IoUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * IO 工具类.
 * <p>
 * 提供统一的流关闭方法，支持各种 IO 资源的安全关闭，避免资源泄漏。
 * </p>
 *
 * <h3>支持的资源类型：</h3>
 * <ul>
 *   <li>{@link InputStream} - 输入流</li>
 *   <li>{@link OutputStream} - 输出流</li>
 *   <li>{@link Reader} - 字符输入流</li>
 *   <li>{@link Writer} - 字符输出流</li>
 *   <li>{@link Channel} - 通道</li>
 *   <li>{@link Closeable} - 所有可关闭的资源</li>
 *   <li>{@link AutoCloseable} - 所有自动关闭的资源</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 关闭单个流
 * InputStream in = new FileInputStream("test.txt");
 * IoUtils.closeQuietly(in);
 *
 * // 关闭多个流
 * InputStream in = new FileInputStream("input.txt");
 * OutputStream out = new FileOutputStream("output.txt");
 * IoUtils.closeQuietly(in, out);
 *
 * // 安静关闭流，关闭异常会记录日志
 * InputStream in = new FileInputStream("test.txt");
 * IoUtils.closeQuietly(in);
 * }</pre>
 *
 * @author zengdegui
 * @see Closeable
 * @see AutoCloseable
 * @since 2024/08/20
 */
@Slf4j
public class IosUtil extends IoUtil {

    private IosUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 安静地关闭一个或多个 {@link Closeable} 资源.
     * <p>
     * 捕获关闭过程中的异常并记录日志，不抛出任何异常。适用于不需要中断业务流程的场景。
     * </p>
     *
     * @param closeables 要关闭的资源数组（可为 null）
     */
    public static void closeQuietly(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    log.warn("关闭资源异常 [{}]: {}", closeable.getClass().getName(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 安静地关闭一个或多个 {@link AutoCloseable} 资源.
     * <p>
     * 捕获关闭过程中的异常并记录日志，不抛出任何异常。适用于不需要中断业务流程的场景。
     * </p>
     *
     * @param autoCloseables 要关闭的资源数组（可为 null）
     */
    public static void closeQuietly(AutoCloseable... autoCloseables) {
        if (autoCloseables == null || autoCloseables.length == 0) {
            return;
        }
        for (AutoCloseable autoCloseable : autoCloseables) {
            if (autoCloseable != null) {
                try {
                    autoCloseable.close();
                } catch (Exception e) {
                    log.warn("关闭资源异常 [{}]: {}", autoCloseable.getClass().getName(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 安静地关闭非标准资源.
     * <p>
     * 适用于第三方资源未实现 {@link Closeable} 或 {@link AutoCloseable} 的场景， 同时支持关闭方法声明受检异常的资源。关闭异常会记录日志，但不会向上抛出。
     * </p>
     *
     * @param resource 要关闭的资源（可为 null）
     * @param closer   资源关闭动作（可为 null）
     * @param <T>      资源类型
     */
    public static <T> void closeQuietly(T resource, CloseAction<? super T> closer) {
        if (resource == null || closer == null) {
            return;
        }
        try {
            closer.close(resource);
        } catch (Exception e) {
            log.warn("关闭资源异常 [{}]: {}", resource.getClass().getName(), e.getMessage(), e);
        }
    }

    /**
     * 定义资源关闭动作，允许关闭方法抛出受检异常。
     *
     * @param <T> 资源类型
     */
    @FunctionalInterface
    public interface CloseAction<T> {

        /**
         * 执行资源关闭动作。
         *
         * @param resource 待关闭资源
         * @throws Exception 关闭异常
         */
        void close(T resource) throws Exception;
    }

    /**
     * 关闭一个或多个 {@link Closeable} 资源，并记录异常日志.
     *
     * @param closeables 要关闭的资源数组（可为 null）
     */
    public static void close(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable == null) {
                continue;
            }
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("{}: {}", closeable.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭一个或多个 {@link AutoCloseable} 资源，并记录异常日志.
     *
     * @param autoCloseables 要关闭的资源数组（可为 null）
     */
    public static void close(AutoCloseable... autoCloseables) {
        if (autoCloseables == null || autoCloseables.length == 0) {
            return;
        }
        for (AutoCloseable autoCloseable : autoCloseables) {
            if (autoCloseable == null) {
                continue;
            }
            try {
                autoCloseable.close();
            } catch (Exception e) {
                log.error("{}: {}", autoCloseable.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭 {@link InputStream} 输入流.
     *
     * @param inputStream 输入流（可为 null）
     */
    public static void closeInputStream(InputStream inputStream) {
        closeQuietly(inputStream);
    }

    /**
     * 关闭 {@link OutputStream} 输出流.
     *
     * @param outputStream 输出流（可为 null）
     */
    public static void closeOutputStream(OutputStream outputStream) {
        closeQuietly(outputStream);
    }

    /**
     * 关闭 {@link Reader} 字符输入流.
     *
     * @param reader 字符输入流（可为 null）
     */
    public static void closeReader(Reader reader) {
        closeQuietly(reader);
    }

    /**
     * 关闭 {@link Writer} 字符输出流.
     *
     * @param writer 字符输出流（可为 null）
     */
    public static void closeWriter(Writer writer) {
        closeQuietly(writer);
    }

    /**
     * 关闭 {@link Channel} 通道.
     *
     * @param channel 通道（可为 null）
     */
    public static void closeChannel(Channel channel) {
        closeQuietly(channel);
    }
}
