/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.operation;

import com.iwindplus.base.redis.support.RedisKeyResolver;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aspectj.lang.JoinPoint;
import org.redisson.api.RAtomicLong;
import org.springframework.cache.interceptor.KeyGenerator;
import reactor.core.publisher.Mono;

/**
 * redisson基本操作.
 *
 * @author zengdegui
 * @since 2026/04/04 11:08
 */
public interface RedissonBaseOperation {

    /**
     * 获取 Redis 配置的前缀.
     *
     * @return String
     */
    String getKeyPrefix();

    /**
     * 获取 Redis 键.
     *
     * @param bizKey       业务键（必填）
     * @param names        键名（可选）
     * @param keyResolver  键解析器（可选）
     * @param joinPoint    AOP 切点（可选）
     * @param keyGenerator 键生成器（可选）
     * @param keys         键值（可选）
     * @return String
     */
    String getRedisKey(String bizKey, String[] names, RedisKeyResolver keyResolver,
        JoinPoint joinPoint, KeyGenerator keyGenerator, String[] keys);

    /**
     * 存储字符串值.
     *
     * @param key   键名
     * @param value 要存储的字符串值
     */
    void set(String key, String value);

    /**
     * 存储字符串值，并设置过期时间.
     *
     * @param key      键名
     * @param value    要存储的字符串值
     * @param duration 过期时间（支持负数或零表示永不过期）
     */
    void set(String key, String value, Duration duration);

    /**
     * 防重复提交（SETNX + TTL）.
     *
     * @param key   键名
     * @param value 要存储的字符串值
     * @param ttl   过期时间
     * @return boolean
     */
    boolean setIfAbsent(String key, String value, Duration ttl);

    /**
     * Mono防重复提交（SETNX + TTL）.
     *
     * @param key   键名
     * @param value 要存储的字符串值
     * @param ttl   过期时间
     * @return boolean
     */
    Mono<Boolean> setIfAbsentMono(String key, String value, Duration ttl);

    /**
     * 获取指定键的字符串值.
     *
     * @param key 键名
     * @return String
     */
    Object get(String key);

    /**
     * 向 Hash 表中设置字段值.
     *
     * @param key     Hash 键名
     * @param hashKey Hash 中的具体字段名
     * @param value   要设置的值（支持泛型）
     */
    <T> void hSet(String key, String hashKey, T value);

    /**
     * 从 Hash 表中获取指定字段的值.
     *
     * @param key     Hash 键名
     * @param hashKey Hash 中的具体字段名
     * @param <T>     泛型
     * @return T
     */
    <T> T hGet(String key, String hashKey);

    /**
     * 获取整个 Hash 表.
     *
     * @param key Hash 键名
     * @param <T> 泛型
     * @return Map
     */
    <T> Map<String, T> hGetAll(String key);

    /**
     * 向列表左侧插入一个或多个元素.
     *
     * @param key    列表键名
     * @param values 要插入的一个或多个元素
     */
    void lPush(String key, Object... values);

    /**
     * 向列表右侧插入一个或多个元素.
     *
     * @param key    列表键名
     * @param values 要插入的一个或多个元素
     */
    void rPush(String key, Object... values);

    /**
     * 从列表左侧弹出一个元素.
     *
     * @param key 列表键名
     * @return Object
     */
    Object lPop(String key);

    /**
     * 从列表右侧弹出一个元素.
     *
     * @param key 列表键名
     * @return Object
     */
    Object rPop(String key);

    /**
     * 获取列表中 [start, end) 范围内的元素.
     *
     * @param key   列表键名
     * @param start 起始索引（包含）
     * @param end   结束索引（不包含）
     * @return List<Object>
     */
    List<Object> lRange(String key, int start, int end);

    /**
     * 向集合中添加一个或多个元素.
     *
     * @param key    集合键名
     * @param values 要添加的一个或多个元素
     */
    void sAdd(String key, Object... values);

    /**
     * 获取集合中的所有元素.
     *
     * @param key 集合键名
     * @return Set<Object>
     */
    Set<Object> sMembers(String key);

    /**
     * 从集合中删除一个或多个元素.
     *
     * @param key    集合键名
     * @param values 要删除的一个或多个元素
     */
    void sRem(String key, Object... values);

    /**
     * 向有序集合中添加一个元素，并指定其分数.
     *
     * @param key   有序集合键名
     * @param score 元素的排序分值
     * @param value 要添加的元素
     */
    void zAdd(String key, double score, Object value);

    /**
     * 获取有序集合中 [start, end] 范围内的元素.
     *
     * @param key   有序集合键名
     * @param start 起始索引（包含）
     * @param end   结束索引（包含）
     * @return Collection<Object>
     */
    Collection<Object> zRange(String key, int start, int end);

    /**
     * 从有序集合中删除一个或多个元素.
     *
     * @param key    有序集合键名
     * @param values 要删除的一个或多个元素
     */
    void zRem(String key, Object... values);

    /**
     * 删除指定的键.
     *
     * @param key 要删除的键名
     * @return boolean
     */
    boolean delete(String key);

    /**
     * 判断指定键是否存在.
     *
     * @param key 键名
     * @return boolean
     */
    boolean exists(String key);

    /**
     * 设置键的过期时间.
     *
     * @param key      键名
     * @param duration 过期时间（支持负数或零表示永不过期）
     * @return boolean
     */
    boolean expire(String key, Duration duration);

    /**
     * 获取键的剩余生存时间（若未设置过期时间则返回 -1，若键不存在则返回 -2）.
     *
     * @param key 键名
     * @return long
     */
    long ttl(String key);

    /**
     * 获取或创建一个原子计数器.
     *
     * @param key 计数器键
     * @return RAtomicLong 原子计数器
     */
    RAtomicLong getAtomicCounter(String key);

    /**
     * 递增指定键的计数器，并返回新值.
     *
     * @param key 计数器键
     * @return long 新的计数值
     */
    long incrementAndGet(String key);

    /**
     * 递增指定键的计数器，并返回新值.
     *
     * @param key   计数器键
     * @param delta 递增量
     * @return long 新的计数值
     */
    long incrementBy(String key, long delta);

    /**
     * 递增指定键的计数器，并设置过期时间.
     *
     * @param key      计数器键
     * @param duration 过期时间（支持负数或零表示永不过期）
     * @return long 新的计数值
     */
    long incrementAndGetWithExpire(String key, Duration duration);

    /**
     * 获取当前计数器值.
     *
     * @param key 计数器键
     * @return long 当前计数值
     */
    long getCounterValue(String key);

    /**
     * 删除指定键的计数器.
     *
     * @param key 计数器键
     */
    void deleteCounter(String key);
}
