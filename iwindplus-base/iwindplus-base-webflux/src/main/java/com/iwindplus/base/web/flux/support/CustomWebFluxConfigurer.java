/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.flux.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwindplus.base.web.flux.domain.property.WebFluxProperty;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.ServerCodecConfigurer.ServerDefaultCodecs;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * webflux观察配置器.
 *
 * @author zengdegui
 * @since 2025/11/14 23:22
 */
public record CustomWebFluxConfigurer(ObjectMapper objectMapper, WebFluxProperty property) implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // 1. 自定义 Jackson 编解码器
        Jackson2JsonDecoder jacksonDecoder = new Jackson2JsonDecoder(objectMapper);
        jacksonDecoder.setMaxInMemorySize(property.getMaxInMemorySize());

        Jackson2JsonEncoder jacksonEncoder = new Jackson2JsonEncoder(objectMapper);
        jacksonEncoder.setStreamingMediaTypes(
            List.of(
                MediaType.APPLICATION_NDJSON,
                MediaType.TEXT_EVENT_STREAM
            )
        );

        // 2. 只往 defaults 里放一次，Spring 会自动把它们插到链前端
        ServerDefaultCodecs defaults = configurer.defaultCodecs();
        defaults.jackson2JsonDecoder(jacksonDecoder);
        defaults.jackson2JsonEncoder(jacksonEncoder);
        defaults.maxInMemorySize(property.getMaxInMemorySize());
        defaults.enableLoggingRequestDetails(property.getEnableLoggingRequestDetails());
    }
}
