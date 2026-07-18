/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.domain.property;

import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 跨域配置相关属性.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "cross")
public class CrossProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.FALSE;

    /**
     * 是否发送cookie信息.
     */
    private Boolean allowCredentials;

    /**
     * 允许访问的客户端域名.
     */
    private List<String> allowedOrigins;

    /**
     * 允许服务端访问的客户端请求头.
     */
    private List<String> allowedHeaders;

    /**
     * 允许访问的方法名.
     */
    private List<String> allowedMethods;

    /**
     * 有效时长.
     */
    @Builder.Default
    private Duration maxAge = Duration.ofSeconds(1800L);
}
