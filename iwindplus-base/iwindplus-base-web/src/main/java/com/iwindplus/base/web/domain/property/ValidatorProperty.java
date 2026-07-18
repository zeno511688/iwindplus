/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.domain.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 校验框架相关属性.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "validator")
public class ValidatorProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * Hibernate Validator有 2 种验证模式 普通模式：failFast = false 快速失败返回模式：failFast = true
     */
    @Builder.Default
    private Boolean failFast = Boolean.TRUE;
}
