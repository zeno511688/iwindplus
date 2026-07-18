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
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Jackson配置相关属性.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "jackson")
public class JacksonProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * mybatis分页配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private MybatisPageConfig mybatisPage = new MybatisPageConfig();

    /**
     * 脱敏配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private SensitiveConfig sensitive = new SensitiveConfig();

    /**
     * 脱敏相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensitiveConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;
    }

    /**
     * mybatis分页相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MybatisPageConfig {

        /**
         * 是否启用分页格式化.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;
    }
}
