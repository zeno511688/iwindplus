/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.swagger.domain.property;

import com.iwindplus.base.domain.constant.CommonConstant.NumberConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * swagger相关属性.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperty {

    /**
     * 服务API配置（控制是否插入数据库）.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private ServerApiConfig serverApi = new ServerApiConfig();

    /**
     * 服务API相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerApiConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 服务名称.
         */
        @Builder.Default
        private String serviceName = "iwindplus-mgt";

        /**
         * 存储服务API的URL.
         */
        @Builder.Default
        private String url = "lb://iwindplus-mgt/inner/serverApi/saveOrEdit";

        /**
         * 最大重试次数.
         */
        @Builder.Default
        private Integer maxRetry = NumberConstant.NUMBER_FIVE;

        /**
         * 重试间隔时间（单位：秒）.
         */
        @Builder.Default
        private Integer retryInterval = NumberConstant.NUMBER_THREE;
    }
}