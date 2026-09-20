/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.auth.infrastructure.configuration;

import com.iwindplus.auth.common.constant.AuthConstant;
import com.iwindplus.auth.common.enums.AuthTokenModeEnum;
import com.iwindplus.auth.infrastructure.client.LoginAuthClient;
import com.iwindplus.auth.infrastructure.extension.BindCodeAuthenticationConverter;
import com.iwindplus.auth.infrastructure.extension.BindCodeAuthenticationProvider;
import com.iwindplus.auth.infrastructure.extension.MailCodeAuthenticationConverter;
import com.iwindplus.auth.infrastructure.extension.MailCodeAuthenticationProvider;
import com.iwindplus.auth.infrastructure.extension.PasswordAuthenticationConverter;
import com.iwindplus.auth.infrastructure.extension.PasswordAuthenticationProvider;
import com.iwindplus.auth.infrastructure.extension.RefreshTokenAuthenticationConverter;
import com.iwindplus.auth.infrastructure.extension.RefreshTokenAuthenticationProvider;
import com.iwindplus.auth.infrastructure.extension.SmsCodeAuthenticationConverter;
import com.iwindplus.auth.infrastructure.extension.SmsCodeAuthenticationProvider;
import com.iwindplus.auth.infrastructure.handler.CustomAuthenticationFailureHandler;
import com.iwindplus.auth.infrastructure.handler.CustomAuthenticationSuccessHandler;
import com.iwindplus.auth.infrastructure.handler.CustomOpaqueTokenIntrospector;
import com.iwindplus.auth.infrastructure.handler.CustomTokenCustomizer;
import com.iwindplus.auth.infrastructure.handler.OpaqueAccessTokenGenerator;
import com.iwindplus.auth.infrastructure.handler.OpaqueRefreshTokenGenerator;
import com.iwindplus.auth.infrastructure.handler.TokenTypeRoutingTokenGenerator;
import com.iwindplus.auth.infrastructure.handler.UnAccessDeniedHandler;
import com.iwindplus.auth.infrastructure.handler.UnAuthenticationEntryPoint;
import com.iwindplus.base.web.support.WebManager;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.Resource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

/**
 * 认证服务端配置.
 *
 * @author zengdegui
 * @since 2020/3/24
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(
    {
        AuthProperty.class,
        ServerApiProperty.class,
    }
)
public class AuthorizationServerConfiguration {

    @Resource
    private AuthProperty authProperty;

    @Resource
    private WebManager webManager;

    @Resource
    private LoginAuthClient loginAuthClient;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private ApplicationEventPublisher publisher;

    /**
     * 创建 SecurityFilterChain.
     *
     * @param http                 http
     * @param authorizationService authorizationService
     * @param tokenGenerator       tokenGenerator
     * @return SecurityFilterChain
     * @throws Exception
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
        HttpSecurity http,
        OAuth2AuthorizationService authorizationService,
        OAuth2TokenGenerator<?> tokenGenerator) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
            .with(authorizationServerConfigurer, serverConfigurer -> {
                // 显式设置自定义令牌生成器，防止配置器创建默认的JwtGenerator（OPAQUE模式下会导致生成JWT令牌）
                serverConfigurer.tokenGenerator(tokenGenerator);
            })
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());

        UnAuthenticationEntryPoint authenticationEntryPoint = new UnAuthenticationEntryPoint(this.webManager);
        UnAccessDeniedHandler accessDeniedHandler = new UnAccessDeniedHandler(this.webManager);

        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(
                    new LoginUrlAuthenticationEntryPoint(AuthConstant.LOGIN_URL)
                )
            ).oauth2ResourceServer(resourceServer -> {
                resourceServer
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler);
                // 根据令牌模式切换：OPAQUE模式使用不透明令牌内省，JWT模式使用JWT解码
                if (AuthTokenModeEnum.OPAQUE.equals(this.authProperty.getTokenMode())) {
                    resourceServer.opaqueToken(opaqueToken ->
                        opaqueToken.introspector(new CustomOpaqueTokenIntrospector(authorizationService))
                    );
                } else {
                    resourceServer.jwt(Customizer.withDefaults());
                }
            }).getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .authorizationEndpoint(authorizationEndpoint ->
                // 自定义授权页面
                authorizationEndpoint.consentPage(AuthConstant.CONSENT_URL)
            )
            .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                .accessTokenRequestConverters(
                    authenticationConverters -> {
                        // 自定义授权模式转换器(Converter)
                        authenticationConverters.remove(1);
                        authenticationConverters.addAll(
                            List.of(
                                new PasswordAuthenticationConverter(),
                                new SmsCodeAuthenticationConverter(),
                                new MailCodeAuthenticationConverter(),
                                new BindCodeAuthenticationConverter(),
                                new RefreshTokenAuthenticationConverter()
                            )
                        );
                    }
                )
                .authenticationProviders(
                    authenticationProviders -> {
                        // 自定义授权模式提供者(Provider)
                        authenticationProviders.remove(1);
                        authenticationProviders.addAll(
                            List.of(
                                new PasswordAuthenticationProvider(authorizationService, tokenGenerator, loginAuthClient, passwordEncoder),
                                new SmsCodeAuthenticationProvider(authorizationService, tokenGenerator, loginAuthClient),
                                new MailCodeAuthenticationProvider(authorizationService, tokenGenerator, loginAuthClient),
                                new BindCodeAuthenticationProvider(authorizationService, tokenGenerator, loginAuthClient),
                                new RefreshTokenAuthenticationProvider(authorizationService, tokenGenerator, authProperty)
                            )
                        );
                    }
                )
                // 自定义成功响应
                .accessTokenResponseHandler(
                    new CustomAuthenticationSuccessHandler(this.authProperty, this.webManager, this.publisher)
                )
                // 自定义失败响应
                .errorResponseHandler(
                    new CustomAuthenticationFailureHandler(this.webManager)
                )
            ).clientAuthentication(oAuth2ClientAuthenticationConfigurer ->
                oAuth2ClientAuthenticationConfigurer.errorResponseHandler(
                    new CustomAuthenticationFailureHandler(this.webManager)
                )
            );
        return http.build();
    }

    /**
     * 创建 AuthorizationServerSettings.
     *
     * @return AuthorizationServerSettings
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    /**
     * 创建 OAuth2TokenGenerator<OAuth2Token>. JWT模式：注册JwtGenerator，生成包含用户信息的JWT令牌. OPAQUE模式：访问令牌使用不透明格式，但保留JwtGenerator用于生成OpenID Connect的ID令牌.
     *
     * @return OAuth2TokenGenerator<OAuth2Token>
     */
    @Bean
    public OAuth2TokenGenerator<OAuth2Token> tokenGenerator() {
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource()));
        jwtGenerator.setJwtCustomizer(new CustomTokenCustomizer());
        // OPAQUE模式：访问令牌和刷新令牌均使用不透明格式（UUID），JwtGenerator仅用于生成ID令牌
        if (AuthTokenModeEnum.OPAQUE.equals(this.authProperty.getTokenMode())) {
            OpaqueAccessTokenGenerator opaqueAccessTokenGenerator = new OpaqueAccessTokenGenerator();
            OpaqueRefreshTokenGenerator opaqueRefreshTokenGenerator = new OpaqueRefreshTokenGenerator();
            return new TokenTypeRoutingTokenGenerator(
                opaqueAccessTokenGenerator, opaqueRefreshTokenGenerator, jwtGenerator);
        }
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        // JWT模式：JwtGenerator优先，生成包含用户信息的JWT令牌
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    /**
     * 创建 AuthenticationManager.
     *
     * @param authenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = this.generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
}
