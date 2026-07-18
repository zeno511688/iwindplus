/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.i18n.domain.property;

import com.iwindplus.base.i18n.domain.constant.I18nConstant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * i18n相关属性.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "spring.messages")
public class I18nProperty extends MessageSourceProperties {

    /**
     * 是否启用远程加载.
     */
    private Boolean enabledRemote = Boolean.TRUE;

    /**
     * nacos分组名称.
     */
    private String group = I18nConstant.I18N_GROUP;

    /**
     * 缓存最大数量.
     */
    private Integer maxCacheSize = 300;
}
