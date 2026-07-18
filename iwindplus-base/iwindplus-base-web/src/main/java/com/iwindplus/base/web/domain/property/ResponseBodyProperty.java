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
        @Builder.Default
        private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCYoIHICETSQ410kSJh6aDoH0LK83GgK+waBzwsvIvIii5cD1j/mosaEf6UM6eTrqgdkBcortYO7+G556l243WnJlLmVKvaF8hY5H+5ozYsDUaamrdnQDwansSt9+54ZzIyqbq/Hh38FPebWv0x5/rap4Zi2YWgKRg0Dg0ONiVrRwIDAQAB";

        /**
         * 私钥.
         */
        @Builder.Default
        private String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJiggcgIRNJDjXSRImHpoOgfQsrzcaAr7BoHPCy8i8iKLlwPWP+aixoR/pQzp5OuqB2QFyiu1g7v4bnnqXbjdacmUuZUq9oXyFjkf7mjNiwNRpqat2dAPBqexK337nhnMjKpur8eHfwU95ta/THn+tqnhmLZhaApGDQODQ42JWtHAgMBAAECgYAUegA/boCRNxfsyiXzPe1lBwCmite1Nf9UlKDpnZghXQyQNEzr6fd8lWpyxqVWZmnOoYzB3AcC9QLzNpXhSe34PFB0BJwgCK1v5GgVVxyUvkakjv/xojwRgEZlYl/HLtGOYdP8SFhcsdNf2OGOBg7YfSGLuFu0nhdYqLa7sempoQJBAM6COlPtPHQqtrm4MdOw4H24HnBrIkgjXdbB8BaGcB4lOrHYYl0jpPchEMrHBibFOEaCY2JVNwVGFiZq8DZUueECQQC9NIO76S3EiCxTZYURbSmWPfDxudtrX9w7F/Zi4RgIvdr47632ZqoIXPpjrpTulxVQX/5NwnS0bNDgnpjKZFonAkBjCwJ+jiGYdYP9vuHm7cY9hbjTog5nGs8+2PUVWJUdYC2ubmF+2kGcZTdwidPhdGVxK8gOuWASH3MwKcnbxPcBAkALakmwYqciPlz+Qxe+L2nc6KvKyb3VQplU72MsIAyFKn81mbBTN2p2yrVIolXV90OP79q18k98Ozx28NqjC17tAkEAjHikBAn25mtjAD6XDNw7I7/lUrm4SSD8nvSZk53ATqhPxAxradUjtTspXArq+PLFBaru6g08IJXqledTx8v+gA==";

        /**
         * 密钥.
         */
        @Builder.Default
        private String key = "Juwd0kEkcgsV2mrcmzxnB1PrQKhWDNho";

        /**
         * 初始化向量.
         */
        @Builder.Default
        private String iv = "bA3xvYRKt4gD";
    }
}
