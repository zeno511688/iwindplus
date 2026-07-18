/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web;

import com.iwindplus.base.util.KeysUtil;
import com.iwindplus.base.web.domain.property.KeyGeneratorProperty;
import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 密钥生成器配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(KeyGeneratorProperty.class)
@ConditionalOnProperty(prefix = "key.generator", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KeyGeneratorConfiguration {

    @Resource
    private KeyGeneratorProperty property;

    /**
     * 创建 KeyGenerator.
     *
     * @return KeyGenerator
     */
    @Bean
    public KeyGenerator keyGenerator() {
        KeyGenerator keyGenerator = (Object target, Method method, Object... params) ->
            KeysUtil.generate(this.property.getEnabledCrypto(), target, method, params);
        log.info("KeyGenerator={}", keyGenerator);
        return keyGenerator;
    }
}
