/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;

/**
 * RestClient配置.
 *
 * @author zengdegui
 * @since 2025/08/24 01:38
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({HttpClientProperty.class})
@ConditionalOnProperty(prefix = "http.client.rest", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RestClientConfiguration {

    @Resource
    private HttpClientProperty property;

    @Resource
    private CloseableHttpClient closeableHttpClient;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ObservationRegistry observationRegistry;

    /**
     * 创建负载均衡 RestClient.Builder.
     *
     * @return RestClient.Builder
     */
    @LoadBalanced
    @Bean("loadBalancedRestClientBuilder")
    public Builder loadBalancedRestClientBuilder() {
        final Builder restClientBuilder = createRestClientBuilder();
        log.info("LoadBalanced RestClient.Builder={}", restClientBuilder);
        return restClientBuilder;
    }

    /**
     * 创建 RestClient.Builder.
     *
     * @return RestClient.Builder
     */
    @Bean
    public Builder restClientBuilder() {
        final Builder restClientBuilder = createRestClientBuilder();
        log.info("RestClient.Builder={}", restClientBuilder);
        return restClientBuilder;
    }

    /**
     * 创建负载均衡 RestClient.
     *
     * @param loadBalancedRestClientBuilder loadBalancedRestClientBuilder
     * @return RestClient
     */
    @Bean("loadBalancedRestClient")
    public RestClient loadBalancedRestClient(@LoadBalanced Builder loadBalancedRestClientBuilder) {
        final RestClient restClient = loadBalancedRestClientBuilder.build();
        log.info("LoadBalanced RestClient={}", restClient);
        return restClient;
    }

    /**
     * 创建RestClient实例.
     *
     * @param restClientBuilder restClientBuilder
     * @return RestClient
     */
    @Primary
    @Bean
    public RestClient restClient(Builder restClientBuilder) {
        final RestClient restClient = restClientBuilder.build();
        log.info("RestClient={}", restClient);
        return restClient;
    }

    private Builder createRestClientBuilder() {
        final Builder builder = RestClient.builder()
            .requestFactory(new HttpComponentsClientHttpRequestFactory(closeableHttpClient))
            .messageConverters(converters -> {
                converters.removeIf(MappingJackson2HttpMessageConverter.class::isInstance);
                converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
            });
        if (property.getEnabledObservation()) {
            builder.observationRegistry(observationRegistry)
                .observationConvention(new DefaultClientRequestObservationConvention());
        }
        return builder;
    }
}
