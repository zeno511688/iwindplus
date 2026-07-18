/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation;

import java.util.List;
import reactor.core.publisher.Mono;

/**
 * redis生成流水号操作.
 *
 * @author zengdegui
 * @since 2026/04/04 12:29
 */
public interface RedissonSerialNumOperation {

    /**
     * 生成流水号.
     *
     * @param businessKey 业务标识（如：order）
     * @return String
     */
    String getSerialNum(String businessKey);

    /**
     * 生成固定日期格式的序列号（每天重置序号，补零长度6位）.
     *
     * @param businessKey 业务标识（如：order）
     * @return String
     */
    String getSerialNumDate(String businessKey);

    /**
     * 生成固定日期格式的序列号（补零长度6位）.
     *
     * @param businessKey 业务标识（如：order）
     * @param dailyReset  是否每天重置序号
     * @return String
     */
    String getSerialNumDate(String businessKey, boolean dailyReset);

    /**
     * 生成序列号.
     *
     * @param businessKey 业务标识（如：order）
     * @param dailyReset  是否每天重置序号
     * @return String
     */
    String getSerialNum(String businessKey, boolean dailyReset);

    /**
     * 生成固定日期格式的序列号.
     *
     * @param businessKey 业务标识（如：order）
     * @param length      补零长度
     * @param dailyReset  是否每天重置序号
     * @return String
     */
    String getSerialNumDate(String businessKey, int length, boolean dailyReset);

    /**
     * 生成序列号.
     *
     * @param businessKey 业务标识（如：order）
     * @param length      补零长度
     * @param dailyReset  是否每天重置序号
     * @return String
     */
    String getSerialNum(String businessKey, int length, boolean dailyReset);

    /**
     * 生成指定日期格式的序列号.
     *
     * @param businessKey 业务标识（如：order）
     * @param datePattern 日期格式（如："yyMMdd"）
     * @param length      补零长度
     * @param dailyReset  是否每天重置序号
     * @return String
     */
    String generateSerialNumDate(
        String businessKey,
        String datePattern,
        int length,
        boolean dailyReset
    );

    /**
     * 批量生成序列号（每天重置，默认6位）.
     *
     * @param businessKey 业务标识
     * @param count       生成数量
     * @return List<String>
     */
    List<String> getBatchSerialNums(String businessKey, int count);

    /**
     * 批量生成日期序列号（每天重置，默认6位）.
     *
     * @param businessKey 业务标识
     * @param count       生成数量
     * @return List<String>
     */
    List<String> getBatchSerialNumDates(String businessKey, int count);

    /**
     * 批量生成序列号.
     *
     * @param businessKey 业务标识
     * @param count       生成数量
     * @param length      补零长度
     * @param dailyReset  是否每天重置
     * @return List<String>
     */
    List<String> getBatchSerialNums(
        String businessKey,
        int count,
        int length,
        boolean dailyReset
    );

    /**
     * 批量生成指定日期格式的序列号.
     *
     * @param businessKey 业务标识
     * @param datePattern 日期格式
     * @param count       生成数量
     * @param length      补零长度
     * @param dailyReset  是否每天重置
     * @return List<String>
     */
    List<String> generateBatchSerialNumDates(
        String businessKey,
        String datePattern,
        int count,
        int length,
        boolean dailyReset
    );

    /**
     * 生成按月重置的序列号（格式：businessKey + yyMM + 6位序号）.
     *
     * @param businessKey 业务标识
     * @return String
     */
    String getSerialNumMonth(String businessKey);

    /**
     * 生成按周重置的序列号（格式：businessKey + YYww + 6位序号）.
     *
     * @param businessKey 业务标识
     * @return String
     */
    String getSerialNumWeek(String businessKey);

    /**
     * 响应式生成序列号（每天重置，默认6位）.
     *
     * @param businessKey 业务标识
     * @return Mono<String>
     */
    Mono<String> getSerialNumReactive(String businessKey);

    /**
     * 响应式生成日期序列号（每天重置，默认6位）.
     *
     * @param businessKey 业务标识
     * @return Mono<String>
     */
    Mono<String> getSerialNumDateReactive(String businessKey);

    /**
     * 响应式生成序列号.
     *
     * @param businessKey 业务标识
     * @param length      补零长度
     * @param dailyReset  是否每天重置
     * @return Mono<String>
     */
    Mono<String> getSerialNumReactive(
        String businessKey,
        int length,
        boolean dailyReset
    );

    /**
     * 响应式生成指定日期格式的序列号.
     *
     * @param businessKey 业务标识
     * @param datePattern 日期格式
     * @param length      补零长度
     * @param dailyReset  是否每天重置
     * @return Mono<String>
     */
    Mono<String> generateSerialNumDateReactive(
        String businessKey,
        String datePattern,
        int length,
        boolean dailyReset
    );

    /**
     * 响应式批量生成序列号（每天重置，默认6位）.
     *
     * @param businessKey 业务标识
     * @param count       生成数量
     * @return Mono<List < String>>
     */
    Mono<List<String>> getBatchSerialNumsReactive(
        String businessKey,
        int count
    );

    /**
     * 响应式批量生成指定日期格式的序列号.
     *
     * @param businessKey 业务标识
     * @param datePattern 日期格式
     * @param count       生成数量
     * @param length      补零长度
     * @param dailyReset  是否每天重置
     * @return Mono<List < String>>
     */
    Mono<List<String>> generateBatchSerialNumDatesReactive(
        String businessKey,
        String datePattern,
        int count,
        int length,
        boolean dailyReset
    );

    /**
     * 重置流水号计数器为0（仅适用于无日期的永久计数器，即 getSerialNum 系列）.
     *
     * @param businessKey 业务标识
     */
    void resetSerialNum(String businessKey);

    /**
     * 重置指定子 key 的流水号计数器为0（适用于日期型计数器）.
     *
     * <p>childKey 为日期字符串，对应不同方法：</p>
     *
     * <ul>
     *     <li>getSerialNumDate → "yyMMdd" 格式，如 "260511"</li>
     *     <li>getSerialNumMonth → "yyMM" 格式，如 "2605"</li>
     *     <li>getSerialNumWeek → "YYww" 格式，如 "2620"</li>
     * </ul>
     *
     * @param businessKey 业务标识
     * @param childKey    子 key（日期字符串）
     */
    void resetSerialNum(String businessKey, String childKey);

    /**
     * 查看当前计数器值（不递增，仅适用于无日期的永久计数器）.
     *
     * @param businessKey 业务标识
     * @return long
     */
    long getCurrentSerialNum(String businessKey);

    /**
     * 查看指定子 key 的当前计数器值（不递增，适用于日期型计数器）.
     *
     * @param businessKey 业务标识
     * @param childKey    子 key（日期字符串）
     * @return long
     */
    long getCurrentSerialNum(String businessKey, String childKey);

    /**
     * 设置计数器初始值（下次生成从 startVal + 1 开始，仅适用于无日期的永久计数器）.
     *
     * @param businessKey 业务标识
     * @param startVal    初始值
     */
    void setSerialNum(String businessKey, long startVal);

    /**
     * 设置指定子 key 的计数器初始值（适用于日期型计数器）.
     *
     * @param businessKey 业务标识
     * @param childKey    子 key（日期字符串）
     * @param startVal    初始值
     */
    void setSerialNum(
        String businessKey,
        String childKey,
        long startVal
    );
}