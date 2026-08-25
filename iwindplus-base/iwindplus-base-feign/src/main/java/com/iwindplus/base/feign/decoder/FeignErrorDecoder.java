/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.decoder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.feign.domain.constant.FeignConstant;
import com.iwindplus.base.feign.domain.enums.FeignErrorResponseFormatEnum;
import com.iwindplus.base.feign.domain.property.FeignProperty;
import com.iwindplus.base.feign.exception.FeignAuthenticationException;
import com.iwindplus.base.feign.exception.FeignBusinessException;
import com.iwindplus.base.feign.exception.FeignRateLimitException;
import com.iwindplus.base.feign.exception.FeignTechnicalException;
import com.iwindplus.base.util.JacksonUtil;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

/**
 * Feign 统一异常解码器
 *
 * @author zengdegui
 * @since 2025/09/28
 */
@Slf4j
public class FeignErrorDecoder extends ErrorDecoder.Default {

    private final FeignProperty property;

    public FeignErrorDecoder(FeignProperty property) {
        this.property = property;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        final FeignProperty.FeignErrorConfig config = property.getError();
        if (!Boolean.TRUE.equals(config.getEnabled())) {
            return super.decode(methodKey, response);
        }
        final CachedResponse cachedResponse = cacheResponse(response, maxResponseBodySize(config));
        final Exception defaultException = super.decode(methodKey, cachedResponse.response());
        if (defaultException instanceof RetryableException
            && Boolean.TRUE.equals(config.getPreserveRetryableException())) {
            return defaultException;
        }
        if (FeignErrorResponseFormatEnum.NONE == config.getResponseFormat()) {
            return defaultException;
        }
        final int status = response.status();
        final RemoteError remoteError = parseRemoteError(cachedResponse.body(), config.getResponseFormat());
        final String message = errorMessage(remoteError, defaultException, status);
        final String responseBody = Boolean.TRUE.equals(config.getIncludeResponseBodyInException())
            ? cachedResponse.body() : null;
        log.warn("Feign request failed, method={}, status={}, message={}, responseBodyTruncated={}",
            methodKey, status, message, cachedResponse.truncated());
        if (status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
            return new FeignAuthenticationException(remoteError.bizCode(), message, defaultException,
                methodKey, status, responseBody);
        }
        if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return new FeignRateLimitException(remoteError.bizCode(), message, defaultException,
                methodKey, status, responseBody);
        }
        if (isBusinessStatus(status)) {
            return new FeignBusinessException(remoteError.bizCode(), message, defaultException,
                methodKey, status, responseBody);
        }
        return new FeignTechnicalException(technicalBizCode(status), message, defaultException,
            methodKey, status, responseBody, isRetryableStatus(status));
    }

    private CachedResponse cacheResponse(Response response, int maxSize) {
        if (response.body() == null) {
            return new CachedResponse(response, null, false);
        }
        try (InputStream inputStream = response.body().asInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
                 Math.min(maxSize, FeignConstant.RESPONSE_BODY_BUFFER_SIZE))) {
            final byte[] buffer = new byte[FeignConstant.RESPONSE_BODY_BUFFER_SIZE];
            int total = 0;
            boolean truncated = false;
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                final int writableLength = Math.min(length, maxSize - total);
                if (writableLength > 0) {
                    outputStream.write(buffer, 0, writableLength);
                    total += writableLength;
                }
                if (writableLength < length || total >= maxSize) {
                    truncated = true;
                    break;
                }
            }
            final byte[] bodyBytes = outputStream.toByteArray();
            final Charset charset = response.charset() == null ? StandardCharsets.UTF_8 : response.charset();
            return new CachedResponse(response.toBuilder().body(bodyBytes).build(),
                new String(bodyBytes, charset), truncated);
        } catch (IOException exception) {
            log.warn("Feign response body cache failed, status={}", response.status(), exception);
            return new CachedResponse(response, null, false);
        }
    }

    private RemoteError parseRemoteError(String responseBody, FeignErrorResponseFormatEnum responseFormat) {
        if (!StringUtils.hasText(responseBody) || FeignErrorResponseFormatEnum.RESULT_VO != responseFormat) {
            return RemoteError.empty();
        }
        try {
            final ResultVO<Object> result = JacksonUtil.parseObject(responseBody, new TypeReference<>() {
            });
            if (result != null && StringUtils.hasText(result.getBizCode())) {
                return new RemoteError(result.getBizCode(), result.getBizMessage());
            }
        } catch (RuntimeException exception) {
            log.error("Feign ResultVO error response parse failed.", exception);
        }
        return RemoteError.empty();
    }

    private int maxResponseBodySize(FeignProperty.FeignErrorConfig config) {
        final Integer maxSize = config.getMaxResponseBodySize();
        return maxSize == null || maxSize <= 0 ? FeignConstant.DEFAULT_MAX_RESPONSE_BODY_SIZE : maxSize;
    }

    private boolean isBusinessStatus(int status) {
        return status >= HttpStatus.BAD_REQUEST.value() && status < HttpStatus.INTERNAL_SERVER_ERROR.value()
            && status != HttpStatus.REQUEST_TIMEOUT.value()
            && status != HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private boolean isRetryableStatus(int status) {
        return status == HttpStatus.REQUEST_TIMEOUT.value()
            || status == HttpStatus.BAD_GATEWAY.value()
            || status == HttpStatus.SERVICE_UNAVAILABLE.value()
            || status == HttpStatus.GATEWAY_TIMEOUT.value();
    }

    private String technicalBizCode(int status) {
        if (status == HttpStatus.SERVICE_UNAVAILABLE.value()
            || status == HttpStatus.BAD_GATEWAY.value()
            || status == HttpStatus.GATEWAY_TIMEOUT.value()) {
            return BizCodeEnum.SERVICE_UNAVAILABLE.getBizCode();
        }
        if (status == HttpStatus.REQUEST_TIMEOUT.value()) {
            return BizCodeEnum.EXECUTE_TIMEOUT.getBizCode();
        }
        return BizCodeEnum.SERVER_ERROR.getBizCode();
    }

    private String errorMessage(RemoteError remoteError, Exception defaultException, int status) {
        if (StringUtils.hasText(remoteError.bizMessage())) {
            return remoteError.bizMessage();
        }
        if (defaultException != null && StringUtils.hasText(defaultException.getMessage())) {
            return defaultException.getMessage();
        }
        final HttpStatus httpStatus = HttpStatus.resolve(status);
        return httpStatus == null ? BizCodeEnum.RPC_ERROR.getBizMessage() : httpStatus.getReasonPhrase();
    }

    private record CachedResponse(Response response, String body, boolean truncated) {
    }

    private record RemoteError(String bizCode, String bizMessage) {

        private static RemoteError empty() {
            return new RemoteError(BizCodeEnum.RPC_ERROR.getBizCode(), null);
        }
    }
}