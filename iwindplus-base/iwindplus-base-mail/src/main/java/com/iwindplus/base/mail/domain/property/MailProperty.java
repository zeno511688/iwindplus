/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.domain.property;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮箱相关属性.
 *
 * @author zengdegui
 * @since 2023/6/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "mail")
public class MailProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 默认配置编码（可选）.
     */
    private String defaultCode;

    /**
     * 是否启用自动故障转移（默认：true）.
     */
    @Builder.Default
    private Boolean enabledFailover = Boolean.TRUE;

    /**
     * 是否开启重试（默认：true）.
     */
    @Builder.Default
    private Boolean enableRetry = Boolean.TRUE;

    /**
     * 初始间隔时间（默认：5秒）.
     */
    @Builder.Default
    private Duration period = Duration.ofSeconds(5);

    /**
     * 最大重试间隔时间（默认：1小时）.
     */
    @Builder.Default
    private Duration maxPeriod = Duration.ofSeconds(3600);

    /**
     * 最大重试次数（默认：5次）.
     */
    @Builder.Default
    private Integer maxAttempts = 5;

    /**
     * 邮件配置列表.
     */
    @Builder.Default
    private List<MailConfig> configs = new ArrayList<>(10);

    /**
     * 邮件基础配置.
     *
     * @author zengdegui
     * @since 2026/9/4
     */
    @EqualsAndHashCode(callSuper = true)
    @Getter
    @Setter
    public static class BaseConfig extends MailProperties {

        /**
         * 是否启用.
         */
        private Boolean enabled = Boolean.TRUE;

        /**
         * 配置编码（唯一标识）.
         */
        private String code;

        /**
         * 配置名称.
         */
        private String name;

        /**
         * 发件人昵称（可选）.
         */
        private String nickName;

        /**
         * 优先级（数字越小优先级越高，用于自动故障转移）.
         */
        private Integer priority;
    }

    /**
     * 邮件配置.
     */
    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = true)
    public static class MailConfig extends BaseConfig {
    }
}

