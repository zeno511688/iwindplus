/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.domain.property;

import java.time.Duration;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 邮箱相关属性.
 *
 * @author zengdegui
 * @since 2023/6/1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperty extends MailProperties {

    /**
     * 发件人昵称（可选）.
     */
    private String nickName;

    /**
     * 是否开启重试.
     */
    @Builder.Default
    private Boolean enableRetry = true;

    /**
     * 初始间隔时间（单位：秒）.
     */
    @Builder.Default
    private Duration period = Duration.ofSeconds(5);

    /**
     * 最大重试间隔时间（单位：秒）.
     */
    @Builder.Default
    private Duration maxPeriod = Duration.ofSeconds(3600);

    /**
     * 最大重试次数.
     */
    @Builder.Default
    private Integer maxAttempts = 5;

    /**
     * 其他参数.
     */
    private Map<String, String> properties;
}

