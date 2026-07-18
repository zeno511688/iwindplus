/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mongo.domain.property;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * mongo相关属性.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "mongo")
public class MongoProperty {

    /**
     * 字段配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private FieldConfig field = new FieldConfig();

    /**
     * 字段相关属性.
     *
     * @author zengdegui
     * @since 2024/4/6
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldConfig {

        /**
         * 填充策略配置.
         */
        @Builder.Default
        @NestedConfigurationProperty
        private FillConfig fill = new FillConfig();

        /**
         * 填充策略相关属性.
         *
         * @author zengdegui
         * @since 2024/4/6
         */
        @Data
        @SuperBuilder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FillConfig {

            /**
             * 是否启用.
             */
            @Builder.Default
            private Boolean enabled = Boolean.TRUE;

            /**
             * 插入是否严格（false时：公共字段可以自定义值）.
             */
            @Builder.Default
            private Boolean enabledInsertStrict = Boolean.TRUE;
        }
    }
}
