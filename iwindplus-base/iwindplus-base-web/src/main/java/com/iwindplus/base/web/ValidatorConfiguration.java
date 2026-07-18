/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web;

import com.iwindplus.base.web.domain.property.ValidatorProperty;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import java.util.Optional;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * 验证器配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(ValidatorProperty.class)
@ConditionalOnProperty(prefix = "validator", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ValidatorConfiguration {

    @Resource
    private ValidatorProperty property;

    /**
     * 配置验证器.
     *
     * @param messageSource messageSource
     * @return Validator
     */
    @Bean
    @ConditionalOnBean(MessageSource.class)
    public Validator validator(MessageSource messageSource) {
        final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        final Properties properties = new Properties();
        final Boolean failFast = Optional.ofNullable(this.property.getFailFast()).orElse(Boolean.FALSE);
        properties.setProperty("hibernate.validator.fail_fast", failFast.toString());
        validator.setValidationProperties(properties);
        log.info("Validator={}", validator);
        return validator;
    }
}
