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
 * 统一响应体配置相关属性.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "response.body")
public class ResponseBodyProperty {

    /**
     * 是否启用.
     */
    @Builder.Default
    private Boolean enabled = Boolean.TRUE;

    /**
     * 忽略的Class.
     */
    private List<String> ignoredClasses;

    /**
     * 响应体加解密安全配置.
     */
    @Builder.Default
    @NestedConfigurationProperty
    private ResponseBodyCryptoConfig crypto = new ResponseBodyCryptoConfig();

    /**
     * 响应体加解密安全相关属性.
     *
     * @author zengdegui
     * @since 2023/6/1
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseBodyCryptoConfig extends CryptoDTO {

        /**
         * 是否启用.
         */
        @Builder.Default
        private Boolean enabled = Boolean.FALSE;

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
