/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.log.domain.property;

import com.iwindplus.base.alert.domain.enums.AlertChannelTypeEnum;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 告警日志相关属性.
 *
 * @author zengdegui
 * @since 2025/11/23 21:18
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "alert.log")
public class AlertLogProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.FALSE;

    /**
     * 所有者.
     */
    private List<String> owners;

    /**
     * 排除的日志表达式.
     */
    private List<String> excludePatterns;

    /**
     * 采样率（10：代表10%的采样率）.
     */
    @Builder.Default
    private Integer sampleRate = 100;

    /**
     * webhook配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private WebhookCfg webhook = new WebhookCfg();

    /**
     * 限流配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private RateLimitCfg rateLimit = new RateLimitCfg();

    /**
     * 堆栈配置
     */
    @Builder.Default
    @NestedConfigurationProperty
    private StackCfg stack = new StackCfg();

    /**
     * webhook相关属性.
     *
     * @author zengdegui
     * @since 2020/4/24
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookCfg {

        /**
         * 告警渠道类型.
         */
        @Builder.Default
        private AlertChannelTypeEnum channelType = AlertChannelTypeEnum.FEI_SHU;

        /**
         * 路径.
         */
        private String url;

        /**
         * 密钥（可选）.
         */
        private String secret;
    }

    /**
     * 限流相关属性.
     * 
     * <p>使用令牌桶算法：
     * <ul>
     *   <li>capacity = maxRequests（桶容量）</li>
     *   <li>rate = maxRequests / windowSeconds（每秒生成的令牌数）</li>
     * </ul>
     *
     * @author zengdegui
     * @since 2020/4/24
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateLimitCfg {

        /**
         * 时间窗口（秒）.
         * 
         * <p>用于计算令牌生成速率：rate = maxRequests / windowSeconds
         */
        @Builder.Default
        private Long windowSeconds = 60L;

        /**
         * 静默时间（秒）.
         * 
         * <p>超过限流后进入静默期，静默期内不再发送告警
         */
        @Builder.Default
        private Long silenceSeconds = 300L;

        /**
         * 最大请求数（桶容量）.
         * 
         * <p>在时间窗口内允许的最大告警次数
         */
        @Builder.Default
        private Integer maxRequests = 10;

        /**
         * 缓存大小.
         * 
         * <p>限流键的最大缓存数量
         */
        @Builder.Default
        private Integer cacheSize = 1000;
    }

    /**
     * 堆栈相关属性.
     *
     * @author zengdegui
     * @since 2020/4/24
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StackCfg {

        /**
         * 最大堆栈长度（字符数），<=0 表示不限制
         */
        @Builder.Default
        private Integer maxLength = 5000;

        /**
         * 最大堆栈帧数，<=0 表示不限制
         */
        @Builder.Default
        private Integer maxFrames = 50;
    }
}
