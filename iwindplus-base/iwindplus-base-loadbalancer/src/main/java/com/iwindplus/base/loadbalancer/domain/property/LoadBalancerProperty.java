/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.loadbalancer.domain.property;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 负载均衡配置属性.
 *
 * @author zengdegui
 * @since 2023/10/24 23:02
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "loadbalancer")
public class LoadBalancerProperty {

    /**
     * Nacos负载均衡配置.
     */
    private NacosConfig nacos = new NacosConfig();

    /**
     * 灰度发布配置.
     */
    private GrayConfig gray = new GrayConfig();

    @Data
    public static class NacosConfig {

        /**
         * 是否启用Nacos负载均衡，默认true.
         */
        private Boolean enabled = true;
    }

    @Data
    public static class GrayConfig {

        /**
         * 是否启用灰度发布，默认false.
         */
        private Boolean enabled = false;

        /**
         * 用户ID请求头名称，默认X-User-Id.
         */
        private String userIdHeader = "X-User-Id";

        /**
         * 灰度版本请求头名称，默认X-Gray-Version.
         */
        private String grayVersionHeader = "X-Gray-Version";

        /**
         * 灰度百分比（0-100），默认10%.
         */
        private Integer percentage = 10;

        /**
         * 灰度用户ID白名单.
         */
        private List<String> userIdWhitelist;

        /**
         * 灰度版本，默认v2.
         */
        private String grayVersion = "v2";
    }
}
