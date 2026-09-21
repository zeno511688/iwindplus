/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.http.client.support;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iwindplus.base.domain.enums.AppCertTypeEnum;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.vo.BaseSignExtendVO;
import com.iwindplus.base.domain.vo.BaseSignVO;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.http.client.domain.enums.HttpClientTypeEnum;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty;
import com.iwindplus.base.http.client.domain.property.HttpClientProperty.ApiProtectionConfig;
import com.iwindplus.base.http.client.factory.HttpClientExecuteHandlerFactory;
import com.iwindplus.base.util.PathMatchUtil;
import com.iwindplus.base.util.SecureRandomUtil;
import com.iwindplus.base.util.domain.dto.ApiSignGenerateDTO;
import com.iwindplus.base.web.domain.property.FilterProperty;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;

/**
 * API防护提供者.
 *
 * @author zengdegui
 * @since 2025/02/15 21:45
 */
@Slf4j
public record ApiProtectionProvider(
    FilterProperty filterProperty,
    HttpClientProperty httpClientProperty,
    HttpClientExecuteHandlerFactory httpClientExecuteHandlerFactory) {

    private static final List<String> DEFAULT_IGNORED_APIS =
        List.of(
            "/favicon.ico",
            "/actuator/**",
            "/health",
            "/metrics",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-resources/**",
            "/webjars/swagger-ui/**",
            "/doc.html"
        );

    private static final AsyncCache<AppCertTypeEnum, BaseSignVO> APP_CERT_CACHE =
        Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(5))
            .buildAsync();

    /**
     * 获取签名配置.
     *
     * @param path 请求路径
     * @return BaseSignExtendVO
     */
    public BaseSignExtendVO loadSignCfg(String path) {
        final ApiProtectionConfig cfg = httpClientProperty.getApiProtection();
        if (Boolean.FALSE.equals(cfg.getEnabled())) {
            return null;
        }

        // 忽略当前远程应用凭证配置的URL
        if (cfg.getPath().contains(path)) {
            return null;
        }

        final Set<String> patters = new HashSet<>(DEFAULT_IGNORED_APIS);
        final List<String> ignoredApi = cfg.getIgnoredApi();
        Optional.ofNullable(ignoredApi).ifPresent(patters::addAll);
        if (PathMatchUtil.match(patters.stream().toList(), path)) {
            return null;
        }

        BaseSignVO cert = null;

        // 远程证书优先
        if (Boolean.TRUE.equals(cfg.getEnabledRemote())) {
            cert = this.getRemoteCert(AppCertTypeEnum.SERVICE_INTERNAL_SIGN, cfg);
        }

        // 本地配置兜底
        if (cert == null) {
            cert = new BaseSignVO(cfg.getAccessKey(), cfg.getSecretKey(), cfg.getTimeout().toSeconds());
        }

        this.validateBaseSignVO(cert);

        return BaseSignExtendVO.builder()
            .accessKey(cert.getAccessKey())
            .secretKey(cert.getSecretKey())
            .timeout(cert.getTimeout())
            .application(SpringUtil.getApplicationName())
            .build();
    }

    /**
     * 构建生成签名对象.
     *
     * @param path   请求路径（相对路径）
     * @param method 方法
     * @return ApiSignGenerateDTO
     */
    public ApiSignGenerateDTO buildSignGenerate(String path, String method) {
        final BaseSignExtendVO entity = this.loadSignCfg(path);
        if (Objects.isNull(entity)) {
            return null;
        }

        final String timestamp = String.valueOf(System.currentTimeMillis());
        final String nonce = SecureRandomUtil.randomString(12);
        return ApiSignGenerateDTO.builder()
            .accessKey(entity.getAccessKey())
            .secretKey(entity.getSecretKey())
            .timestamp(timestamp)
            .nonce(nonce)
            .path(path)
            .method(method)
            .application(entity.getApplication())
            .build();
    }

    private BaseSignVO getRemoteCert(AppCertTypeEnum appCertType, ApiProtectionConfig cfg) {
        // 远程凭证统一使用本地缓存，AsyncCache 自动管理异步加载
        CompletableFuture<BaseSignVO> future = APP_CERT_CACHE.get(
            appCertType,
            (key, executor) -> loadRemoteCertAsync(appCertType, cfg)
        );
        return future.join();
    }

    private CompletableFuture<BaseSignVO> loadRemoteCertAsync(AppCertTypeEnum appCertType, ApiProtectionConfig cfg) {
        log.info("加载远程服务间调用应用凭证配置: {}", appCertType);
        final Map<String, ?> query = Map.of(
            "appCertType", appCertType
        );
        // 使用 WebClient 异步请求，避免在 reactor 线程中触发 block() 异常
        CompletionStage<ResultVO<BaseSignVO>> stage = httpClientExecuteHandlerFactory
            .getHandler(HttpClientTypeEnum.WEB_CLIENT)
            .getAsync(
                cfg.getUrl(),
                query,
                null,
                new TypeReference<>() {
                }
            );
        return stage.toCompletableFuture().thenApply(result -> {
            result.errorThrow();
            BaseSignVO cert = result.getBizData();
            validateBaseSignVO(cert);
            return cert;
        });
    }

    private void validateBaseSignVO(BaseSignVO vo) {
        if (vo == null) {
            throw new BizException(BizCodeEnum.APP_CERT_CONFIG_NOT_EXIST);
        }

        if (CharSequenceUtil.isBlank(vo.getAccessKey())) {
            throw new BizException(BizCodeEnum.ACCESS_KEY_NOT_EXIST);
        }

        if (CharSequenceUtil.isBlank(vo.getSecretKey())) {
            throw new BizException(BizCodeEnum.SECRET_KEY_NOT_EXIST);
        }

        if (vo.getTimeout() == null) {
            throw new BizException(BizCodeEnum.SIGN_TIMEOUT_NOT_EXIST);
        }
    }
}