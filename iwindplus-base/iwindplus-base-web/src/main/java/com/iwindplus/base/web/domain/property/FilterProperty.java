/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.domain.property;

import com.iwindplus.base.domain.enums.AlgorithmTypeEnum;
import com.iwindplus.base.util.domain.dto.CryptoDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 过滤器相关属性.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "filter")
public class FilterProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * xss过滤器配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private XssFilterConfig xss = new XssFilterConfig();

    /**
     * 请求过滤器配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private RequestFilterConfig request = new RequestFilterConfig();

    /**
     * 过滤器加解密安全配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private FilterCryptoConfig crypto = new FilterCryptoConfig();

    /**
     * xss过滤器相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class XssFilterConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 是否启用跳过.
         */
        @Builder.Default
        private Boolean enabledSkip = Boolean.FALSE;

        /**
         * 标签白名单（需要忽略的标签）.
         */
        private List<String> tagWhiteList;

        /**
         * 忽略的API（不需要xss过滤）.
         */
        private List<String> ignoredApi;

        /**
         * 忽略的符号.
         */
        private List<String> ignoredSymbol;
    }

    /**
     * 请求过滤器相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestFilterConfig {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;
    }

    /**
     * 过滤器加解密安全相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterCryptoConfig extends CryptoDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.TRUE;

        /**
         * 算法.
         */
        @Builder.Default
        private AlgorithmTypeEnum algorithm = AlgorithmTypeEnum.AES;

        /**
         * 公钥.
         */
        private String publicKey;

        /**
         * 私钥.
         */
        private String privateKey;

        /**
         * 密钥.
         */
        private String key;
    }
}
