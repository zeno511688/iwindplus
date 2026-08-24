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
     * 灰度发布配置.
     */
    private GrayConfig gray = new GrayConfig();

    @Data
    public static class GrayConfig {

        /**
         * 是否启用灰度发布，默认false.
         */
        private Boolean enabled = false;

        /**
         * 灰度用户ID白名单.
         */
        private List<String> userIdWhitelist;

        /**
         * 灰度百分比（0-100），默认10%.
         */
        private Integer percentage = 10;
    }
}
