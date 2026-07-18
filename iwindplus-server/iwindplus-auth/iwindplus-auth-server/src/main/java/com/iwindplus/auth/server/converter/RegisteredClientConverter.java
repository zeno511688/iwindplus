package com.iwindplus.auth.server.converter;

import com.iwindplus.base.domain.enums.TimeToLiveUnitEnum;
import com.iwindplus.mgt.domain.dto.system.ClientSettingDTO;
import com.iwindplus.mgt.domain.dto.system.TokenSettingDTO;
import com.iwindplus.mgt.domain.vo.system.ClientVO;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * 客户端对象转换器.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Component
public class RegisteredClientConverter {

    /**
     * 转换
     *
     * @param source 源
     * @return ClientVO
     */
    public ClientVO convert(RegisteredClient source) {
        if (null == source) {
            return null;
        }
        return ClientVO.builder()
            .id(Long.valueOf(source.getId()))
            .clientId(source.getClientId())
            .clientIdIssuedAt(RegisteredClientConverter.instantToTime(source.getClientIdIssuedAt()))
            .clientSecret(source.getClientSecret())
            .clientSecretExpiresAt(RegisteredClientConverter.instantToTime(source.getClientSecretExpiresAt()))
            .clientName(source.getClientName())
            .redirectUri(source.getRedirectUris())
            .logoutRedirectUri(source.getPostLogoutRedirectUris())
            .authenticationMethod(source.getClientAuthenticationMethods()
                .stream().filter(Objects::nonNull).map(ClientAuthenticationMethod::getValue)
                .collect(Collectors.toSet()))
            .authorizedGrantType(source.getAuthorizationGrantTypes()
                .stream().filter(Objects::nonNull).map(AuthorizationGrantType::getValue)
                .collect(Collectors.toSet()))
            .scope(source.getScopes())
            .clientSetting(RegisteredClientConverter.resolveClientSettings(source.getClientSettings()))
            .tokenSetting(RegisteredClientConverter.resolveTokenSettings(source.getTokenSettings()))
            .build();
    }

    /**
     * 转换
     *
     * @param source 源
     * @return RegisteredClient
     */
    public RegisteredClient convert(ClientVO source) {
        if (null == source) {
            return null;
        }
        final RegisteredClient.Builder builder = RegisteredClient.withId(source.getId().toString());
        builder.clientId(source.getClientId())
            .clientIdIssuedAt(RegisteredClientConverter.timeToInstant(source.getClientIdIssuedAt()))
            .clientSecret(source.getClientSecret())
            .clientSecretExpiresAt(RegisteredClientConverter.timeToInstant(source.getClientSecretExpiresAt()))
            .clientName(source.getClientName())
            .redirectUris(uris -> uris.addAll(source.getRedirectUri()))
            .postLogoutRedirectUris(uris -> uris.addAll(source.getLogoutRedirectUri()))
            .clientSettings(RegisteredClientConverter.resolveOauthClientSettings(source.getClientSetting(), source.getAuthenticationMethod()))
            .tokenSettings(RegisteredClientConverter.resolveOauthTokenSettings(source.getTokenSetting()))
            .clientAuthenticationMethods(methods ->
                methods.addAll(
                    source.getAuthenticationMethod()
                        .stream().filter(Objects::nonNull).map(ClientAuthenticationMethod::new)
                        .collect(Collectors.toSet())
                )
            ).authorizationGrantTypes(grantTypes ->
                grantTypes.addAll(
                    source.getAuthorizedGrantType()
                        .stream().filter(Objects::nonNull).map(AuthorizationGrantType::new)
                        .collect(Collectors.toSet())
                )
            ).scopes(scopes -> scopes.addAll(source.getScope()));
        return builder.build();
    }

    static LocalDateTime instantToTime(Instant instant) {
        if (null == instant) {
            return null;
        }
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    static Instant timeToInstant(LocalDateTime time) {
        if (null == time) {
            return null;
        }
        return time.atZone(ZoneId.systemDefault()).toInstant();
    }

    static ClientSettingDTO resolveClientSettings(ClientSettings clientSettings) {
        ClientSettingDTO model = new ClientSettingDTO();
        if (null == clientSettings) {
            return model;
        }
        model.setJwkSetUrl(clientSettings.getJwkSetUrl());
        model.setRequireProofKey(clientSettings.isRequireProofKey());
        model.setRequireAuthorizationConsent(clientSettings.isRequireAuthorizationConsent());
        model.setX509CertificateSubjectDN(clientSettings.getX509CertificateSubjectDN());
        if (!ObjectUtils.isEmpty(clientSettings.getTokenEndpointAuthenticationSigningAlgorithm())) {
            model.setTokenEndpointAuthenticationSigningAlgorithm(clientSettings.getTokenEndpointAuthenticationSigningAlgorithm().getName());
        }
        return model;
    }

    static ClientSettings resolveOauthClientSettings(ClientSettingDTO clientSettings, Set<String> clientAuthenticationMethods) {
        ClientSettings.Builder builder = ClientSettings.builder();
        if (null == clientSettings) {
            return builder.build();
        }
        builder.requireProofKey(null == clientSettings.getRequireProofKey() ? Boolean.FALSE : clientSettings.getRequireProofKey());
        builder.requireAuthorizationConsent(
            null == clientSettings.getRequireAuthorizationConsent() ? Boolean.FALSE : clientSettings.getRequireAuthorizationConsent());
        if (!ObjectUtils.isEmpty(clientSettings.getTokenEndpointAuthenticationSigningAlgorithm())
            && !ObjectUtils.isEmpty(clientAuthenticationMethods)) {
            if (clientAuthenticationMethods.contains(ClientAuthenticationMethod.CLIENT_SECRET_JWT.getValue())) {
                MacAlgorithm macAlgorithm = MacAlgorithm.from(clientSettings.getTokenEndpointAuthenticationSigningAlgorithm());
                if (null == macAlgorithm) {
                    macAlgorithm = MacAlgorithm.HS256;
                }
                builder.tokenEndpointAuthenticationSigningAlgorithm(macAlgorithm);
            } else if (clientAuthenticationMethods.contains(ClientAuthenticationMethod.PRIVATE_KEY_JWT.getValue())) {
                SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.from(clientSettings.getTokenEndpointAuthenticationSigningAlgorithm());
                if (null == signatureAlgorithm) {
                    signatureAlgorithm = SignatureAlgorithm.RS256;
                }
                builder.tokenEndpointAuthenticationSigningAlgorithm(signatureAlgorithm);
                if (!ObjectUtils.isEmpty(clientSettings.getJwkSetUrl())) {
                    builder.jwkSetUrl(clientSettings.getJwkSetUrl());
                }
            }
        }
        if (!ObjectUtils.isEmpty(clientSettings.getX509CertificateSubjectDN())) {
            builder.x509CertificateSubjectDN(clientSettings.getX509CertificateSubjectDN());
        }
        return builder.build();
    }

    static TokenSettingDTO resolveTokenSettings(TokenSettings tokenSettings) {
        TokenSettingDTO model = new TokenSettingDTO();
        if (null == tokenSettings) {
            return model;
        }
        // 授权码有效时长
        model.setAuthorizationCodeTimeToLive(tokenSettings.getAuthorizationCodeTimeToLive().toSeconds());
        // 默认以秒的形式保存
        model.setAuthorizationCodeTimeToLiveUnit(TimeToLiveUnitEnum.SECONDS);
        // access token 有效时长
        model.setAccessTokenTimeToLive(tokenSettings.getAccessTokenTimeToLive().toSeconds());
        // 默认以秒的形式保存
        model.setAccessTokenTimeToLiveUnit(TimeToLiveUnitEnum.SECONDS);
        // access token的格式
        model.setAccessTokenFormat(tokenSettings.getAccessTokenFormat().getValue());
        // 设备码有效时长
        model.setDeviceCodeTimeToLive(tokenSettings.getDeviceCodeTimeToLive().toSeconds());
        // 默认以秒的形式保存
        model.setDeviceCodeTimeToLiveUnit(TimeToLiveUnitEnum.SECONDS);
        // 设置refresh token是否可重复使用
        model.setReuseRefreshTokens(tokenSettings.isReuseRefreshTokens());
        // 设置refresh token是否可重复使用
        model.setRefreshTokenTimeToLive(tokenSettings.getRefreshTokenTimeToLive().toSeconds());
        // 默认以秒的形式保存
        model.setRefreshTokenTimeToLiveUnit(TimeToLiveUnitEnum.SECONDS);
        // 对ID Token进行签名的JWS算法。
        model.setIdTokenSignatureAlgorithm(tokenSettings.getIdTokenSignatureAlgorithm().getName());
        // 如果访问令牌必须绑定到客户端x509使用tls_client_auth或self_signed_tls_client_auth方法进行客户端身份验证期间接收的证书，则设置为true。
        model.setX509CertificateBoundAccessTokens(tokenSettings.isX509CertificateBoundAccessTokens());
        return model;
    }

    static TokenSettings resolveOauthTokenSettings(TokenSettingDTO tokenSettings) {
        TokenSettings.Builder builder = TokenSettings.builder();
        if (null == tokenSettings) {
            return builder.build();
        }
        // 授权码有效时长
        if (!ObjectUtils.isEmpty(tokenSettings.getAuthorizationCodeTimeToLive())) {
            builder.authorizationCodeTimeToLive(
                Duration.of(tokenSettings.getAuthorizationCodeTimeToLive(), tokenSettings.getAuthorizationCodeTimeToLiveUnit().getUnit()));
        }
        // access token 有效时长
        if (!ObjectUtils.isEmpty(tokenSettings.getAccessTokenTimeToLive())) {
            builder.accessTokenTimeToLive(
                Duration.of(tokenSettings.getAccessTokenTimeToLive(), tokenSettings.getAccessTokenTimeToLiveUnit().getUnit()));
        }
        // access token的格式
        if (tokenSettings.getAccessTokenFormat().equals(OAuth2TokenFormat.SELF_CONTAINED.getValue())) {
            builder.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED);
        } else if (tokenSettings.getAccessTokenFormat().equals(OAuth2TokenFormat.REFERENCE.getValue())) {
            builder.accessTokenFormat(OAuth2TokenFormat.REFERENCE);
        } else {
            // 默认使用jwt token
            builder.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED);
        }
        // 设备码有效时长
        if (!ObjectUtils.isEmpty(tokenSettings.getDeviceCodeTimeToLive())) {
            builder.deviceCodeTimeToLive(Duration.of(tokenSettings.getDeviceCodeTimeToLive(), tokenSettings.getDeviceCodeTimeToLiveUnit().getUnit()));
        }
        // 设置refresh token是否可重复使用
        if (!ObjectUtils.isEmpty(tokenSettings.getReuseRefreshTokens())) {
            builder.reuseRefreshTokens(tokenSettings.getReuseRefreshTokens());
        }
        // refresh token有效时长
        if (!ObjectUtils.isEmpty(tokenSettings.getRefreshTokenTimeToLive())) {
            builder.refreshTokenTimeToLive(
                Duration.of(tokenSettings.getRefreshTokenTimeToLive(), tokenSettings.getRefreshTokenTimeToLiveUnit().getUnit()));
        }
        // 对ID Token进行签名的JWS算法。
        if (!ObjectUtils.isEmpty(tokenSettings.getIdTokenSignatureAlgorithm())) {
            SignatureAlgorithm algorithm = SignatureAlgorithm.from(tokenSettings.getIdTokenSignatureAlgorithm());
            if (null == algorithm) {
                algorithm = SignatureAlgorithm.RS256;
            }
            builder.idTokenSignatureAlgorithm(algorithm);
        }
        // 如果访问令牌必须绑定到客户端x509使用tls_client_auth或self_signed_tls_client_auth方法进行客户端身份验证期间接收的证书，则设置为true。
        if (!ObjectUtils.isEmpty(tokenSettings.getX509CertificateBoundAccessTokens())) {
            builder.x509CertificateBoundAccessTokens(tokenSettings.getX509CertificateBoundAccessTokens());
        }
        return builder.build();
    }
}
