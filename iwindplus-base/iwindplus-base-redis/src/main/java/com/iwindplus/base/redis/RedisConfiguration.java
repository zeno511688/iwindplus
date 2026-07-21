/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis;

import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.redis.aspect.RedisIdempotentAspect;
import com.iwindplus.base.redis.aspect.RedisLockAspect;
import com.iwindplus.base.redis.aspect.RedisRateLimiterAspect;
import com.iwindplus.base.redis.aspect.RedisRepeatSubmitAspect;
import com.iwindplus.base.redis.domain.property.RedisProperty;
import com.iwindplus.base.redis.operation.RedissonBaseOperation;
import com.iwindplus.base.redis.operation.RedissonIdempotentOperation;
import com.iwindplus.base.redis.operation.RedissonLockOperation;
import com.iwindplus.base.redis.operation.RedissonRateLimiterOperation;
import com.iwindplus.base.redis.operation.RedissonRepeatSubmitOperation;
import com.iwindplus.base.redis.operation.RedissonSerialNumOperation;
import com.iwindplus.base.redis.operation.impl.RedissonBaseOperationImpl;
import com.iwindplus.base.redis.operation.impl.RedissonIdempotentOperationImpl;
import com.iwindplus.base.redis.operation.impl.RedissonLockOperationImpl;
import com.iwindplus.base.redis.operation.impl.RedissonRateLimiterOperationImpl;
import com.iwindplus.base.redis.operation.impl.RedissonRepeatSubmitOperationImpl;
import com.iwindplus.base.redis.operation.impl.RedissonSerialNumOperationImpl;
import com.iwindplus.base.redis.service.RedissonService;
import com.iwindplus.base.redis.service.impl.RedissonServiceImpl;
import com.iwindplus.base.redis.support.handler.ExceptionCacheErrorHandler;
import com.iwindplus.base.redis.support.impl.ClientIpRedisKeyResolver;
import com.iwindplus.base.redis.support.impl.DefaultRedisKeyResolver;
import com.iwindplus.base.redis.support.impl.ServerNodeRedisKeyResolver;
import com.iwindplus.base.redis.support.impl.UserRedisKeyResolver;
import com.iwindplus.base.redis.support.serializer.GzipRedisSerializer;
import com.iwindplus.base.redis.support.serializer.KryoRedisSerializer;
import com.iwindplus.base.redis.support.serializer.PrefixRedisSerializer;
import com.iwindplus.base.redis.support.serializer.ProtostuffRedisSerializer;
import com.iwindplus.base.redis.support.strategy.CustomLockFailureStrategy;
import com.iwindplus.base.redis.support.strategy.CustomLockKeyBuilder;
import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.CoreJackson2Module;

/**
 * Redis配置.
 *
 * @author zengdegui
 * @since 2018/9/5
 */
@Slf4j
@Configuration
@EnableCaching
@EnableConfigurationProperties({CacheProperties.class, RedisProperty.class})
public class RedisConfiguration {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Resource
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @Resource
    private CacheProperties cacheProperties;

    @Resource
    private RedisProperty property;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 创建 RedisTemplate.
     *
     * @param keyRedisSerializer   keyRedisSerializer
     * @param valueRedisSerializer valueRedisSerializer
     * @return RedisTemplate<?, ?>
     */
    @Bean
    public RedisTemplate<?, ?> redisTemplate(PrefixRedisSerializer keyRedisSerializer, GzipRedisSerializer<Object> valueRedisSerializer) {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.redisConnectionFactory);
        redisTemplate.setKeySerializer(keyRedisSerializer);
        redisTemplate.setValueSerializer(valueRedisSerializer);
        redisTemplate.setHashKeySerializer(keyRedisSerializer);
        redisTemplate.setHashValueSerializer(valueRedisSerializer);
        redisTemplate.afterPropertiesSet();
        log.info("RedisTemplate={}", redisTemplate);
        return redisTemplate;
    }

    /**
     * 创建ReactiveRedisTemplate.
     *
     * @param keyRedisSerializer   keyRedisSerializer
     * @param valueRedisSerializer valueRedisSerializer
     * @return ReactiveRedisTemplate<?, ?>
     */
    @Bean
    public ReactiveRedisTemplate<?, ?> reactiveRedisTemplate(PrefixRedisSerializer keyRedisSerializer,
        GzipRedisSerializer<Object> valueRedisSerializer) {
        RedisSerializationContext<?, ?> serializationContext = RedisSerializationContext.<String, Object>newSerializationContext()
            .key(keyRedisSerializer).value(valueRedisSerializer)
            .hashKey(keyRedisSerializer).hashValue(valueRedisSerializer)
            .build();
        ReactiveRedisTemplate<?, ?> reactiveRedisTemplate = new ReactiveRedisTemplate<>(this.reactiveRedisConnectionFactory,
            serializationContext);
        log.info("ReactiveRedisTemplate={}", reactiveRedisTemplate);
        return reactiveRedisTemplate;
    }

    /**
     * 创建 CacheManager.
     *
     * @param keyRedisSerializer   keyRedisSerializer
     * @param valueRedisSerializer valueRedisSerializer
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager(PrefixRedisSerializer keyRedisSerializer, GzipRedisSerializer<Object> valueRedisSerializer) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keyRedisSerializer))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueRedisSerializer));
        CacheProperties.Redis redisProperties = this.cacheProperties.getRedis();
        if (Objects.nonNull(redisProperties.getTimeToLive())) {
            redisCacheConfiguration = redisCacheConfiguration.entryTtl(redisProperties.getTimeToLive());
        }
        if (CharSequenceUtil.isNotBlank(redisProperties.getKeyPrefix())) {
            redisCacheConfiguration = redisCacheConfiguration.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            redisCacheConfiguration = redisCacheConfiguration.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            redisCacheConfiguration = redisCacheConfiguration.disableKeyPrefix();
        }
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(this.redisConnectionFactory,
            BatchStrategies.scan(this.property.getScanBatchSize()));
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisCacheWriter)
            .cacheDefaults(redisCacheConfiguration)
            .build();
        log.info("CacheManager={}", redisCacheManager);
        return redisCacheManager;
    }

    /**
     * 创建 PrefixRedisSerializer.
     *
     * @param redissonBaseOperation 基本操作
     * @return PrefixRedisSerializer
     */
    @Bean
    public PrefixRedisSerializer keyRedisSerializer(RedissonBaseOperation redissonBaseOperation) {
        String keyPrefix = redissonBaseOperation.getKeyPrefix();
        PrefixRedisSerializer prefixRedisSerializer = new PrefixRedisSerializer(keyPrefix);
        log.info("PrefixRedisSerializer={}", prefixRedisSerializer);
        return prefixRedisSerializer;
    }

    /**
     * 创建 GzipRedisSerializer.
     *
     * @return GzipRedisSerializer<?>
     */
    @Bean
    public GzipRedisSerializer<?> valueRedisSerializer() {
        GzipRedisSerializer<?> serializer = new GzipRedisSerializer<>(this.getRedisSerializer());
        log.info("GzipRedisSerializer={}", serializer);
        return serializer;
    }

    /**
     * 创建 ExceptionCacheErrorHandler.
     *
     * @return ExceptionCacheErrorHandler
     */
    @Bean
    public ExceptionCacheErrorHandler exceptionCacheErrorHandler() {
        ExceptionCacheErrorHandler exceptionCacheErrorHandler = new ExceptionCacheErrorHandler();
        log.info("ExceptionCacheErrorHandler={}", exceptionCacheErrorHandler);
        return exceptionCacheErrorHandler;
    }

    /**
     * 创建 DefaultRedisKeyResolver.
     *
     * @return DefaultRedisKeyResolver
     */
    @Primary
    @Bean
    public DefaultRedisKeyResolver defaultRedisKeyResolver() {
        DefaultRedisKeyResolver defaultRedisKeyResolver = new DefaultRedisKeyResolver();
        log.info("DefaultRedisKeyResolver={}", defaultRedisKeyResolver);
        return defaultRedisKeyResolver;
    }

    /**
     * 创建 UserRedisKeyResolver.
     *
     * @return UserRedisKeyResolver
     */
    @Bean
    public UserRedisKeyResolver userRedisKeyResolver() {
        UserRedisKeyResolver userRedisKeyResolver = new UserRedisKeyResolver();
        log.info("UserRedisKeyResolver={}", userRedisKeyResolver);
        return userRedisKeyResolver;
    }

    /**
     * 创建 ClientIpRedisKeyResolver.
     *
     * @return ClientIpRedisKeyResolver
     */
    @Bean
    public ClientIpRedisKeyResolver clientIpRedisKeyResolver() {
        ClientIpRedisKeyResolver clientIpRedisKeyResolver = new ClientIpRedisKeyResolver();
        log.info("ClientIpRedisKeyResolver={}", clientIpRedisKeyResolver);
        return clientIpRedisKeyResolver;
    }

    /**
     * 创建 ServerNodeRedisKeyResolver.
     *
     * @return ServerNodeRedisKeyResolver
     */
    @Bean
    public ServerNodeRedisKeyResolver serverNodeRedisKeyResolver() {
        ServerNodeRedisKeyResolver serverNodeRedisKeyResolver = new ServerNodeRedisKeyResolver();
        log.info("ServerNodeRedisKeyResolver={}", serverNodeRedisKeyResolver);
        return serverNodeRedisKeyResolver;
    }

    /**
     * 创建 RedissonService.
     *
     * @return RedissonService
     */
    @Bean
    public RedissonService redissonService() {
        RedissonService redissonService = new RedissonServiceImpl();
        log.info("RedissonService={}", redissonService);
        return redissonService;
    }

    /**
     * 创建 RedissonBaseOperation.
     *
     * @return RedissonBaseOperation
     */
    @Bean
    public RedissonBaseOperation redissonBaseOperation() {
        RedissonBaseOperation redissonBaseOperation = new RedissonBaseOperationImpl();
        log.info("RedissonBaseOperation={}", redissonBaseOperation);
        return redissonBaseOperation;
    }

    /**
     * 创建 RedissonRepeatSubmitOperation.
     *
     * @return RedissonRepeatSubmitOperation
     */
    @Bean
    public RedissonRepeatSubmitOperation redissonRepeatSubmitOperation() {
        RedissonRepeatSubmitOperation redissonRepeatSubmitOperation = new RedissonRepeatSubmitOperationImpl();
        log.info("RedissonRepeatSubmitOperation={}", redissonRepeatSubmitOperation);
        return redissonRepeatSubmitOperation;
    }

    /**
     * 创建 RedisRepeatSubmitAspect.
     *
     * @return RedisRepeatSubmitAspect
     */
    @Bean
    public RedisRepeatSubmitAspect redisRepeatSubmitAspect() {
        RedisRepeatSubmitAspect redisRepeatSubmitAspect = new RedisRepeatSubmitAspect();
        log.info("RedisRepeatSubmitAspect={}", redisRepeatSubmitAspect);
        return redisRepeatSubmitAspect;
    }

    /**
     * 创建 RedissonIdempotentOperation.
     *
     * @return RedissonIdempotentOperation
     */
    @Bean
    public RedissonIdempotentOperation redissonIdempotentOperation() {
        RedissonIdempotentOperation redissonIdempotentOperation = new RedissonIdempotentOperationImpl();
        log.info("RedissonIdempotentOperation={}", redissonIdempotentOperation);
        return redissonIdempotentOperation;
    }

    /**
     * 创建 RedisIdempotentAspect.
     *
     * @return RedisIdempotentAspect
     */
    @Bean
    public RedisIdempotentAspect redisIdempotentAspect() {
        RedisIdempotentAspect redisIdempotentAspect = new RedisIdempotentAspect();
        log.info("RedisIdempotentAspect={}", redisIdempotentAspect);
        return redisIdempotentAspect;
    }

    /**
     * 创建 RedissonLockOperation.
     *
     * @return RedissonLockOperation
     */
    @Bean
    public RedissonLockOperation redissonLockOperation() {
        RedissonLockOperation redissonLockOperation = new RedissonLockOperationImpl();
        log.info("RedissonLockOperation={}", redissonLockOperation);
        return redissonLockOperation;
    }

    /**
     * 创建 RedisLockAspect.
     *
     * @return RedisLockAspect
     */
    @Bean
    public RedisLockAspect redisLockAspect() {
        RedisLockAspect redisLockAspect = new RedisLockAspect();
        log.info("RedisLockAspect={}", redisLockAspect);
        return redisLockAspect;
    }

    /**
     * 创建 RedissonRateLimiterOperation.
     *
     * @return RedissonRateLimiterOperation
     */
    @Bean
    public RedissonRateLimiterOperation redissonRateLimiterOperation() {
        RedissonRateLimiterOperation redissonRateLimiterOperation = new RedissonRateLimiterOperationImpl();
        log.info("RedissonRateLimiterOperation={}", redissonRateLimiterOperation);
        return redissonRateLimiterOperation;
    }

    /**
     * 创建 RedisRateLimiterAspect.
     *
     * @return RedisRateLimiterAspect
     */
    @Bean
    public RedisRateLimiterAspect redisRateLimiterAspect() {
        RedisRateLimiterAspect redisRateLimiterAspect = new RedisRateLimiterAspect();
        log.info("RedisRateLimiterAspect={}", redisRateLimiterAspect);
        return redisRateLimiterAspect;
    }

    /**
     * 创建 RedissonSerialNumOperation.
     *
     * @return RedissonSerialNumOperation
     */
    @Bean
    public RedissonSerialNumOperation redissonSerialNumOperation() {
        RedissonSerialNumOperation redissonSerialNumOperation = new RedissonSerialNumOperationImpl();
        log.info("RedissonSerialNumOperation={}", redissonSerialNumOperation);
        return redissonSerialNumOperation;
    }

    /**
     * 创建 CustomLockFailureStrategy.
     *
     * @return CustomLockFailureStrategy
     */
    @Bean
    public CustomLockFailureStrategy customLockFailureStrategy() {
        CustomLockFailureStrategy customLockFailureStrategy = new CustomLockFailureStrategy();
        log.info("CustomLockFailureStrategy={}", customLockFailureStrategy);
        return customLockFailureStrategy;
    }

    /**
     * 创建 CustomLockKeyBuilder.
     *
     * @return CustomLockKeyBuilder
     */
    @Bean
    public CustomLockKeyBuilder customLockKeyBuilder() {
        CustomLockKeyBuilder customLockKeyBuilder = new CustomLockKeyBuilder();
        log.info("CustomLockKeyBuilder={}", customLockKeyBuilder);
        return customLockKeyBuilder;
    }

    private RedisSerializer<?> getRedisSerializer() {
        switch (this.property.getSerializeType()) {
            case KRYO:
                KryoRedisSerializer kryoSerializer = new KryoRedisSerializer();
                log.info("KryoRedisSerializer={}", kryoSerializer);
                return kryoSerializer;
            case PROTOBUF:
                ProtostuffRedisSerializer protostuffSerializer = new ProtostuffRedisSerializer();
                log.info("ProtostuffRedisSerializer={}", protostuffSerializer);
                return protostuffSerializer;
            case JACKSON:
                final ObjectMapper redisMapper = this.objectMapper.copy();
                redisMapper.activateDefaultTyping(redisMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, As.PROPERTY);
                redisMapper.registerModules(Arrays.asList(new CoreJackson2Module()));
                final Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(redisMapper,
                    Object.class);
                log.info("Jackson2JsonRedisSerializer={}", jackson2JsonRedisSerializer);
                return jackson2JsonRedisSerializer;
            case JDK:
                final JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
                log.info("JdkSerializationRedisSerializer={}", jdkSerializationRedisSerializer);
                return jdkSerializationRedisSerializer;
            default:
                throw new BizException(BizCodeEnum.UNSUPPORTED_TYPE, new Object[]{this.property.getSerializeType()});
        }
    }
}
