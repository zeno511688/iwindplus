/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.es;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.es.domain.property.EsProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * es配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(EsProperty.class)
public class EsConfiguration {

    /**
     * es json mapper.
     *
     * @param objectMapper objectMapper
     * @return JacksonJsonpMapper
     */
    @Bean
    public JacksonJsonpMapper jacksonJsonpMapper(ObjectMapper objectMapper) {
        return new JacksonJsonpMapper(objectMapper);
    }
}
