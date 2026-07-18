/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.util.JacksonUtil;
import com.iwindplus.base.web.domain.property.JacksonProperty;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * jackson相关配置.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
@Slf4j
@Configuration
@AutoConfigureBefore(JacksonAutoConfiguration.class)
@EnableConfigurationProperties(JacksonProperty.class)
@ConditionalOnProperty(prefix = "jackson", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JacksonConfiguration {

    @Resource
    private JacksonProperties jacksonProperties;

    @Resource
    private JacksonProperty property;

    /**
     * 创建 objectMapper.
     *
     * @param jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer
     * @return ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer) {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        jackson2ObjectMapperBuilderCustomizer.customize(builder);
        ObjectMapper objectMapper = builder.build();
        log.info("ObjectMapper={}", objectMapper);
        return objectMapper;
    }

    /**
     * 创建 jackson2ObjectMapperBuilderCustomizer.
     *
     * @return Jackson2ObjectMapperBuilderCustomizer
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        final Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer =
            JacksonUtil.jackson2ObjectMapperBuilderCustomizer(
                jacksonProperties,
                property.getSensitive().getEnabled(),
                property.getMybatisPage().getEnabled());
        log.info("Jackson2ObjectMapperBuilderCustomizer={}", jackson2ObjectMapperBuilderCustomizer);
        return jackson2ObjectMapperBuilderCustomizer;
    }

    /**
     * 初始化 JacksonUtil.
     *
     * @param objectMapper objectMapper
     * @return Object
     */
    @Bean("jacksonUtilInitializer")
    public Object jacksonUtilInitializer(ObjectMapper objectMapper) {
        JacksonUtil.setObjectMapper(objectMapper);
        log.info("jacksonUtilInitializer");
        return "jacksonUtilInitializer";
    }
}
