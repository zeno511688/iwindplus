/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant.DateConstant;
import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.operation.RedissonBaseOperation;
import com.iwindplus.base.redis.operation.RedissonSerialNumOperation;
import jakarta.annotation.Resource;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;

/**
 * Redis流水号生成器（去锁 + Lua原子 + 高并发安全）.
 *
 * @author zengdegui
 * @since 2026/04/04 12:58
 */
@Slf4j
public class RedissonSerialNumOperationImpl implements RedissonSerialNumOperation {

    @Resource
    private RedissonBaseOperation redissonBaseOperation;

    @Resource
    private RedissonClient redissonClient;

    private static final String DEFAULT_BIZ_KEY = "default";

    private static final String SERIAL_NUM_KEY_PREFIX = "serial:num:";

    private static final String LUA_INCR_WITH_TTL =
        "if redis.call('exists', KEYS[1]) == 0 then " +
            "redis.call('set', KEYS[1], 0); " +
            "redis.call('expire', KEYS[1], ARGV[1]); " +
            "else " +
            "if redis.call('ttl', KEYS[1]) == -1 then " +
            "redis.call('expire', KEYS[1], ARGV[1]); " +
            "end " +
            "end; " +
            "return redis.call('incr', KEYS[1]);";

    private static final String LUA_INCRBY_WITH_TTL =
        "if redis.call('exists', KEYS[1]) == 0 then " +
            "redis.call('set', KEYS[1], 0); " +
            "redis.call('expire', KEYS[1], ARGV[1]); " +
            "else " +
            "if redis.call('ttl', KEYS[1]) == -1 then " +
            "redis.call('expire', KEYS[1], ARGV[1]); " +
            "end " +
            "end; " +
            "return redis.call('incrby', KEYS[1], ARGV[2]);";

    @Override
    public String getSerialNum(String businessKey) {
        return getSerialNum(businessKey, true);
    }

    @Override
    public String getSerialNumDate(String businessKey) {
        return getSerialNumDate(businessKey, true);
    }

    @Override
    public String getSerialNumDate(String businessKey, boolean dailyReset) {
        return getSerialNumDate(businessKey, NumberConstant.NUMBER_SIX, dailyReset);
    }

    @Override
    public String getSerialNum(String businessKey, boolean dailyReset) {
        return getSerialNum(businessKey, NumberConstant.NUMBER_SIX, dailyReset);
    }

    @Override
    public String getSerialNumDate(String businessKey, int length, boolean dailyReset) {
        return generateSerialNumDate(
            businessKey,
            DateConstant.YYMMDD,
            length,
            dailyReset
        );
    }

    @Override
    public String getSerialNum(String businessKey, int length, boolean dailyReset) {
        return generate(businessKey, null, length, dailyReset);
    }

    @Override
    public String generateSerialNumDate(String businessKey,
        String datePattern,
        int length,
        boolean dailyReset) {

        return generate(
            businessKey,
            formatDate(datePattern),
            length,
            dailyReset
        );
    }

    @Override
    public List<String> getBatchSerialNums(String businessKey, int count) {
        return getBatchSerialNums(
            businessKey,
            count,
            NumberConstant.NUMBER_SIX,
            true
        );
    }

    @Override
    public List<String> getBatchSerialNumDates(String businessKey, int count) {
        return generateBatchSerialNumDates(
            businessKey,
            DateConstant.YYMMDD,
            count,
            NumberConstant.NUMBER_SIX,
            true
        );
    }

    @Override
    public List<String> getBatchSerialNums(String businessKey,
        int count,
        int length,
        boolean dailyReset) {

        return generateBatch(
            businessKey,
            null,
            count,
            length,
            dailyReset
        );
    }

    @Override
    public List<String> generateBatchSerialNumDates(String businessKey,
        String datePattern,
        int count,
        int length,
        boolean dailyReset) {

        return generateBatch(
            businessKey,
            formatDate(datePattern),
            count,
            length,
            dailyReset
        );
    }

    @Override
    public String getSerialNumMonth(String businessKey) {
        String date = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyMM"));

        return generate(
            businessKey,
            date,
            NumberConstant.NUMBER_SIX,
            secondsToNextMonth()
        );
    }

    @Override
    public String getSerialNumWeek(String businessKey) {
        String date = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("YYww"));

        return generate(
            businessKey,
            date,
            NumberConstant.NUMBER_SIX,
            secondsToNextWeek()
        );
    }

    @Override
    public Mono<String> getSerialNumReactive(String businessKey) {
        return getSerialNumReactive(
            businessKey,
            NumberConstant.NUMBER_SIX,
            true
        );
    }

    @Override
    public Mono<String> getSerialNumDateReactive(String businessKey) {
        return generateSerialNumDateReactive(
            businessKey,
            DateConstant.YYMMDD,
            NumberConstant.NUMBER_SIX,
            true
        );
    }

    @Override
    public Mono<String> getSerialNumReactive(String businessKey,
        int length,
        boolean dailyReset) {

        return generateReactive(
            businessKey,
            null,
            length,
            dailyReset
        );
    }

    @Override
    public Mono<String> generateSerialNumDateReactive(String businessKey,
        String datePattern,
        int length,
        boolean dailyReset) {

        return generateReactive(
            businessKey,
            formatDate(datePattern),
            length,
            dailyReset
        );
    }

    @Override
    public Mono<List<String>> getBatchSerialNumsReactive(String businessKey,
        int count) {

        return generateBatchReactive(
            businessKey,
            null,
            count,
            NumberConstant.NUMBER_SIX,
            true
        );
    }

    @Override
    public Mono<List<String>> generateBatchSerialNumDatesReactive(String businessKey,
        String datePattern,
        int count,
        int length,
        boolean dailyReset) {

        return generateBatchReactive(
            businessKey,
            formatDate(datePattern),
            count,
            length,
            dailyReset
        );
    }

    @Override
    public void resetSerialNum(String businessKey) {
        redissonBaseOperation.deleteCounter(
            buildKey(normalizeBizKey(businessKey), null)
        );
    }

    @Override
    public void resetSerialNum(String businessKey, String childKey) {
        redissonBaseOperation.deleteCounter(
            buildKey(normalizeBizKey(businessKey), childKey)
        );
    }

    @Override
    public long getCurrentSerialNum(String businessKey) {
        return redissonBaseOperation.getCounterValue(
            buildKey(normalizeBizKey(businessKey), null)
        );
    }

    @Override
    public long getCurrentSerialNum(String businessKey, String childKey) {
        return redissonBaseOperation.getCounterValue(
            buildKey(normalizeBizKey(businessKey), childKey)
        );
    }

    @Override
    public void setSerialNum(String businessKey, long startVal) {
        redissonBaseOperation.getAtomicCounter(
            buildKey(normalizeBizKey(businessKey), null)
        ).set(startVal);
    }

    @Override
    public void setSerialNum(String businessKey,
        String childKey,
        long startVal) {

        redissonBaseOperation.getAtomicCounter(
            buildKey(normalizeBizKey(businessKey), childKey)
        ).set(startVal);
    }

    private String generate(String businessKey,
        String childBusinessKey,
        int length,
        boolean dailyReset) {

        String bizKey = normalizeBizKey(businessKey);
        String incKey = buildKey(bizKey, childBusinessKey);
        String prefix = buildPrefix(bizKey, childBusinessKey);

        log.debug("流水号生成 key={}", incKey);

        return prefix + padLeft(
            incrWithLua(incKey, dailyReset),
            length
        );
    }

    private String generate(String businessKey,
        String childBusinessKey,
        int length,
        long ttlSeconds) {

        String bizKey = normalizeBizKey(businessKey);
        String incKey = buildKey(bizKey, childBusinessKey);
        String prefix = buildPrefix(bizKey, childBusinessKey);

        log.debug("流水号生成 key={}", incKey);

        return prefix + padLeft(
            incrWithLuaTtl(incKey, ttlSeconds),
            length
        );
    }

    private List<String> generateBatch(String businessKey,
        String childBusinessKey,
        int count,
        int length,
        boolean dailyReset) {

        if (count <= 0 || length <= 0) {
            throw new BizException(BizCodeEnum.PARAM_ERROR);
        }

        String bizKey = normalizeBizKey(businessKey);
        String incKey = buildKey(bizKey, childBusinessKey);
        String prefix = buildPrefix(bizKey, childBusinessKey);

        log.debug("批量流水号生成 key={} count={}", incKey, count);

        return buildBatchList(
            prefix,
            incrByWithLua(incKey, count, dailyReset),
            count,
            length
        );
    }

    private Mono<String> generateReactive(String businessKey,
        String childBusinessKey,
        int length,
        boolean dailyReset) {

        if (length <= 0) {
            return Mono.error(new BizException(BizCodeEnum.PARAM_ERROR));
        }

        String bizKey = normalizeBizKey(businessKey);
        String incKey = buildKey(bizKey, childBusinessKey);
        String prefix = buildPrefix(bizKey, childBusinessKey);

        log.debug("流水号生成(reactive) key={}", incKey);

        return incrWithLuaReactive(incKey, dailyReset)
            .map(value -> prefix + padLeft(value, length));
    }

    private Mono<List<String>> generateBatchReactive(String businessKey,
        String childBusinessKey,
        int count,
        int length,
        boolean dailyReset) {

        if (count <= 0 || length <= 0) {
            return Mono.error(new BizException(BizCodeEnum.PARAM_ERROR));
        }

        String bizKey = normalizeBizKey(businessKey);
        String incKey = buildKey(bizKey, childBusinessKey);
        String prefix = buildPrefix(bizKey, childBusinessKey);

        log.debug("批量流水号生成(reactive) key={} count={}", incKey, count);

        return incrByWithLuaReactive(incKey, count, dailyReset)
            .map(maxValue -> buildBatchList(
                prefix,
                maxValue,
                count,
                length
            ));
    }

    private long incrWithLua(String key, boolean dailyReset) {
        if (!dailyReset) {
            return redissonBaseOperation.incrementAndGet(key);
        }

        return incrWithLuaTtl(key, secondsToNextDay());
    }

    private long incrWithLuaTtl(String key, long ttlSeconds) {
        return redissonClient.getScript(StringCodec.INSTANCE).eval(
            Mode.READ_WRITE,
            LUA_INCR_WITH_TTL,
            RScript.ReturnType.INTEGER,
            Collections.singletonList(key),
            ttlSeconds
        );
    }

    private long incrByWithLua(String key,
        int count,
        boolean dailyReset) {

        if (!dailyReset) {
            return redissonBaseOperation
                .getAtomicCounter(key)
                .addAndGet(count);
        }

        return redissonClient.getScript(StringCodec.INSTANCE).eval(
            Mode.READ_WRITE,
            LUA_INCRBY_WITH_TTL,
            RScript.ReturnType.INTEGER,
            Collections.singletonList(key),
            secondsToNextDay(),
            count
        );
    }

    private Mono<Long> incrWithLuaReactive(String key,
        boolean dailyReset) {

        if (!dailyReset) {
            return redissonClient.reactive()
                .getAtomicLong(key)
                .incrementAndGet();
        }

        return redissonClient.reactive()
            .getScript(StringCodec.INSTANCE)
            .eval(
                Mode.READ_WRITE,
                LUA_INCR_WITH_TTL,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(key),
                secondsToNextDay()
            );
    }

    private Mono<Long> incrByWithLuaReactive(String key,
        int count,
        boolean dailyReset) {

        if (!dailyReset) {
            return redissonClient.reactive()
                .getAtomicLong(key)
                .addAndGet(count);
        }

        return redissonClient.reactive()
            .getScript(StringCodec.INSTANCE)
            .eval(
                Mode.READ_WRITE,
                LUA_INCRBY_WITH_TTL,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(key),
                secondsToNextDay(),
                (long) count
            );
    }

    private String normalizeBizKey(String businessKey) {
        return CharSequenceUtil.blankToDefault(
            businessKey,
            DEFAULT_BIZ_KEY
        );
    }

    private String formatDate(String datePattern) {
        return LocalDateTime.now().format(
            DateTimeFormatter.ofPattern(
                Optional.ofNullable(datePattern)
                    .orElse(DateConstant.YYMMDD)
            )
        );
    }

    private List<String> buildBatchList(String prefix,
        long maxValue,
        int count,
        int length) {

        long startValue = maxValue - count + 1;

        List<String> result = new ArrayList<>(count);

        for (long i = startValue; i <= maxValue; i++) {
            result.add(prefix + padLeft(i, length));
        }

        return result;
    }

    private String buildKey(String biz, String child) {
        return CharSequenceUtil.isBlank(child)
            ? SERIAL_NUM_KEY_PREFIX + biz
            : SERIAL_NUM_KEY_PREFIX + biz + ":" + child;
    }

    private String buildPrefix(String biz, String child) {
        return CharSequenceUtil.isBlank(child)
            ? biz
            : biz + child;
    }

    private String padLeft(long value, int length) {
        String str = Long.toString(value);

        int len = length - str.length();

        if (len <= 0) {
            return str;
        }

        char[] padding = new char[len];
        Arrays.fill(padding, '0');

        return new String(padding) + str;
    }

    private long secondsToNextDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = LocalDate.now()
            .plusDays(1)
            .atStartOfDay();

        return Duration.between(now, next).getSeconds();
    }

    private long secondsToNextMonth() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime next = LocalDate.now()
            .plusMonths(1)
            .withDayOfMonth(1)
            .atStartOfDay();

        return Duration.between(now, next).getSeconds();
    }

    private long secondsToNextWeek() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime next = LocalDate.now()
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            .atStartOfDay();

        return Duration.between(now, next).getSeconds();
    }
}