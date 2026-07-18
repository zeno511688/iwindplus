/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.feign.aspect.FeignGlobalFallbackAspect;
import com.iwindplus.base.feign.decoder.FeignErrorDecoder;
import com.iwindplus.base.feign.decoder.FeignResponseDecoder;
import com.iwindplus.base.feign.domain.property.FeignProperty;
import com.iwindplus.base.feign.interceptor.FeignRequestInterceptor;
import com.iwindplus.base.web.domain.property.FilterProperty;
import com.iwindplus.base.web.domain.property.ResponseBodyProperty;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import feign.jackson.JacksonDecoder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;

/**
 * feign 配置.
 *
 * @author zengdegui
 * @since 2019/9/3
 */
@Slf4j
@AutoConfigureBefore(FeignAutoConfiguration.class)
@EnableConfigurationProperties({FilterProperty.class, FeignProperty.class, ResponseBodyProperty.class})
public class FeignConfiguration {

    @Resource
    private FeignProperty property;

    @Resource
    private ResponseBodyProperty responseBodyProperty;

    @Resource
    private ObjectMapper objectMapper;

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    /**
     * 创建 feignRequestInterceptor.
     *
     * @return FeignRequestInterceptor
     */
    @Bean
    public FeignRequestInterceptor feignRequestInterceptor() {
        FeignRequestInterceptor feignRequestInterceptor = new FeignRequestInterceptor();
        log.info("Feign RequestInterceptor={}", feignRequestInterceptor);
        return feignRequestInterceptor;
    }

    /**
     * 创建 feignErrorDecoder.
     *
     * @return ErrorDecoder
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        FeignErrorDecoder decoder = new FeignErrorDecoder(this.property);
        log.info("Feign ErrorDecoder={}", decoder);
        return decoder;
    }

    /**
     * 创建 feignEncoder.
     *
     * @return Encoder
     */
    @Bean
    public Encoder feignEncoder() {
        Encoder encoder = new SpringFormEncoder(new SpringEncoder(this.messageConverters));
        log.info("Feign Encoder={}", encoder);
        return encoder;
    }

    /**
     * 创建 feignDecoder.
     *
     * @return Decoder
     */
    @Bean
    public Decoder feignDecoder() {
        final FeignResponseDecoder decoder = new FeignResponseDecoder(new JacksonDecoder(objectMapper), objectMapper, this.responseBodyProperty);
        log.info("Feign Decoder={}", decoder);
        return decoder;
    }

    /**
     * 创建 FeignGlobalFallbackAspect.
     *
     * @return FeignGlobalFallbackAspect
     */
    @Bean
    public FeignGlobalFallbackAspect feignGlobalFallbackAspect() {
        final FeignGlobalFallbackAspect feignGlobalFallbackAspect = new FeignGlobalFallbackAspect();
        log.info("FeignGlobalFallbackAspect={}", feignGlobalFallbackAspect);
        return feignGlobalFallbackAspect;
    }
}
