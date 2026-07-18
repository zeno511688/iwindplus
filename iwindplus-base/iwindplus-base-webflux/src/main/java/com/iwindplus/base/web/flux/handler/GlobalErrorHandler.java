/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.flux.handler;

import com.iwindplus.base.domain.exception.CommonException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.util.ExceptionUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.util.ReactorUtil;
import com.iwindplus.base.web.support.WebManager;
import jakarta.annotation.Resource;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * reactor 端全局异常处理.
 *
 * @author zengdegui
 * @since 2024/06/19 23:08
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "global.error", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GlobalErrorHandler implements WebExceptionHandler {

    @Resource
    private WebManager webManager;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        final String realIp = HttpsUtil.getRealIp(exchange);
        final URI uri = exchange.getRequest().getURI();
        final String className = ex.getClass().getName();

        final HttpStatusCode httpStatusCode;
        final ResultVO<Object> result;
        if (ex instanceof CommonException exception) {
            httpStatusCode = HttpStatus.OK;
            result = ResultVO.error(exception);

            log.warn("自定义异常捕获, realIp={}, uri={}, className={}，status={}，code={}, message={}"
                , realIp, uri, className, httpStatusCode, result.getBizCode(), getMessage(result));
        } else if (ex instanceof ResponseStatusException exception) {
            final HttpStatus httpStatus = HttpStatus.valueOf(exception.getStatusCode().value());
            httpStatusCode = httpStatus;
            result = ResultVO.error(httpStatus);

            log.error("响应状态异常捕获, realIp={}, uri={}, className={}，status={}，code={}，message={}"
                , realIp, uri, className, httpStatusCode, result.getBizCode(), getMessage(result), ex);
        } else {
            final ResponseEntity<ResultVO<Object>> exception = ExceptionUtil.getException(ex, className);
            httpStatusCode = exception.getStatusCode();
            result = exception.getBody();

            if (serverErrorFlag(httpStatusCode)) {
                log.error("兜底异常捕获, realIp={}, uri={}, className={}，status={}，code={}, message={}"
                    , realIp, uri, className, httpStatusCode, result.getBizCode(), getMessage(result), ex);
            } else {
                log.warn("兜底异常捕获, realIp={}, uri={}, className={}，status={}，code={}, message={}"
                    , realIp, uri, className, httpStatusCode, result.getBizCode(), getMessage(result), ex);
            }
        }

        this.webManager.encryptResult(result);
        return ReactorUtil.getMonoResponse(exchange, httpStatusCode, result);
    }

    private boolean serverErrorFlag(HttpStatusCode statusCode) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value() <= statusCode.value();
    }

    private String getMessage(ResultVO<Object> result) {
        return ResultVO.message(result.getBizCode(), result.getBizMessageParams(), result.getBizMessage(), null);
    }
}