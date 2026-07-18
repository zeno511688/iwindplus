/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 客户端设置数据传输对象.
 *
 * @author zengdegui
 * @since 2024/12/30 23:24
 */
@Schema(description = "客户端设置数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSettingDTO implements Serializable {

    /**
     * 授权码流程时是否需要提供challenge and verifier (PKCE模式)
     */
    @Schema(description = """
            授权码流程时是否需要提供challenge and verifier (PKCE模式)
        """)
    private Boolean requireProofKey;

    /**
     * 客户端是否需要授权确认
     */
    @Schema(title = "客户端是否需要授权确认")
    private Boolean requireAuthorizationConsent;

    /**
     * 客户端jwks的url地址
     */
    @Schema(title = "客户端jwks的url地址")
    private String jwkSetUrl;

    /**
     * JWS算法，该算法必须用于签名用于在令牌端点为private_key_jwt和client_secret_jwt身份验证方法对客户端进行身份验证的JWT.
     */
    @Schema(description = """
            JWS算法，该算法必须用于签名用于在令牌端点为private_key_jwt和client_secret_jwt身份验证方法对客户端进行身份验证的JWT。
            private_key_jwt：SignatureAlgorithm
            client_secret_jwt：MacAlgorithm
        """)
    private String tokenEndpointAuthenticationSigningAlgorithm;

    /**
     * 使用tls_client_auth方法进行客户端身份验证时收到的证书，返回与客户端关联的预期主题专有名称。
     */
    @Schema(description = """
            使用tls_client_auth方法进行客户端身份验证时收到的证书，返回与客户端关联的预期主题专有名称。
        """)
    private String x509CertificateSubjectDN;
}
