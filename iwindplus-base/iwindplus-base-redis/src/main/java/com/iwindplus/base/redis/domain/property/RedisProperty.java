/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.property;

import com.iwindplus.base.redis.domain.enums.RedisLockTypeEnum;
import com.iwindplus.base.redis.domain.enums.RedisSerializeTypeEnum;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.redisson.api.RateType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * redis配置.
 *
 * @author zengdegui
 * @since 2020/4/24
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperty {

    /**
     * redis scan 一次返回数量
     */
    @Builder.Default
    private Integer scanBatchSize = 30;

    /**
     * 序列化类型.
     */
    @Builder.Default
    private RedisSerializeTypeEnum serializeType = RedisSerializeTypeEnum.JACKSON;

    /**
     * 限流配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private RateLimiterConfig rateLimiter = new RateLimiterConfig();

    /**
     * 防重复提交配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private RepeatSubmitConfig repeatSubmit = new RepeatSubmitConfig();

    /**
     * 幂等配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private IdempotentConfig idempotent = new IdempotentConfig();

    /**
     * 分布式锁配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private LockConfig lock = new LockConfig();

    /**
     * 限流相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimiterConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 限流类型（可选，默认：OVERALL）.
         */
        @Builder.Default
        private RateType rateType = RateType.OVERALL;

        /**
         * 限流次数，每个时间窗口允许请求数量（可选，默认：1000）.
         */
        @Builder.Default
        private Long rate = 1000L;

        /**
         * 限流速率（可选，默认：1s）.
         */
        @Builder.Default
        private Duration rateInterval = Duration.ofSeconds(1);
    }

    /**
     * 防重复相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepeatSubmitConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 过期时间.
         */
        @Builder.Default
        private Duration ttl = Duration.ofSeconds(5);
    }


    /**
     * 幂等相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdempotentConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 是否启用续期
         */
        @Builder.Default
        private Boolean enabledHeartbeat = Boolean.FALSE;

        /**
         * 幂等处理中过期时间（可选，默认：30s）.
         */
        @Builder.Default
        private Duration processingTtl = Duration.ofSeconds(30);

        /**
         * 幂等成功过期时间（可选，默认：600s）.
         */
        @Builder.Default
        private Duration successTtl = Duration.ofSeconds(600);
    }

    /**
     * 分布式锁相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LockConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 分布式锁类型（可选，默认：LOCK） 目前支持的类型请查看{@link RedisLockTypeEnum}
         *
         * @return RedissonLockType
         */
        @Builder.Default
        private RedisLockTypeEnum lockType = RedisLockTypeEnum.LOCK;

        /**
         * 等待获取锁的最长时间（可选，默认：1）
         *
         * @return long
         */
        @Builder.Default
        private long waitTime = 1;

        /**
         * 租约时间（可选，默认：-1） 如果当前线程成功获取到锁，那么锁将被持有的时间长度。这个时间过后，锁会自动释放，默认为 -1(代表不指定，如果指定则看门狗(watchdog)不会自动续约).
         *
         * @return long
         */
        @Builder.Default
        private long leaseTime = -1;
    }
}
