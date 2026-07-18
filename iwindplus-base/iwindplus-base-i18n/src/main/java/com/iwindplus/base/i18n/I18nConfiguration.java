/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.i18n;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.iwindplus.base.i18n.domain.property.I18nProperty;
import com.iwindplus.base.i18n.support.NacosBundleMessageSource;
import jakarta.annotation.Resource;
import java.nio.charset.Charset;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.executor.DtpExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 国际化配置.
 *
 * @author zengdegui
 * @since 2019/9/3
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.messages", name = "enabled-remote", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(I18nProperty.class)
@ConditionalOnBean(NacosConfigManager.class)
public class I18nConfiguration {

    @Resource
    private DtpExecutor i18nTaskExecutor;

    @Resource
    private I18nProperty property;

    @Resource
    private NacosConfigManager nacosConfigManager;

    /**
     * 创建 MessageSource.
     *
     * @return MessageSource
     */
    @Primary
    @Bean
    public NacosBundleMessageSource messageSource() {
        NacosBundleMessageSource messageSource = NacosBundleMessageSource.builder()
            .configService(this.nacosConfigManager.getConfigService())
            .group(property.getGroup())
            .cacheDuration(this.property.getCacheDuration())
            .maxCacheSize(property.getMaxCacheSize())
            .i18nTaskExecutor(this.i18nTaskExecutor)
            .build();
        messageSource.setBasenames(this.property.getBasename());
        if (Objects.isNull(this.property.getEncoding())) {
            messageSource.setDefaultEncoding(Charset.defaultCharset().name());
        } else {
            messageSource.setDefaultEncoding(this.property.getEncoding().name());
        }
        messageSource.setFallbackToSystemLocale(this.property.isFallbackToSystemLocale());
        messageSource.setAlwaysUseMessageFormat(this.property.isAlwaysUseMessageFormat());
        messageSource.setUseCodeAsDefaultMessage(this.property.isUseCodeAsDefaultMessage());
        log.info("MessageSource={}", messageSource);
        return messageSource;
    }

}
