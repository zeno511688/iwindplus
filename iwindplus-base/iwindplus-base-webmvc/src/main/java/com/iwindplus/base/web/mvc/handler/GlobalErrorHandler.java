/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.web.mvc.handler;

import com.iwindplus.base.domain.exception.CommonException;
import com.iwindplus.base.domain.vo.ResultVO;
import com.iwindplus.base.util.ExceptionUtil;
import com.iwindplus.base.util.HttpsUtil;
import com.iwindplus.base.web.support.WebManager;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * web端全局异常处理.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@ConditionalOnProperty(prefix = "global.error", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GlobalErrorHandler {

    @Resource
    private WebManager webManager;

    /**
     * 全局异常捕获.
     *
     * @param ex      异常
     * @param request 请求
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception ex, HttpServletRequest request) {
        final String realIp = HttpsUtil.getRealIp(request);
        final String uri = request.getRequestURI();
        final String className = ex.getClass().getName();

        final HttpStatusCode httpStatusCode;
        final ResultVO<Object> result;
        if (ex instanceof CommonException exception) {
            httpStatusCode = HttpStatus.OK;
            result = ResultVO.error(exception);

            log.warn("自定义异常捕获, realIp={}, uri={}, className={}，status={}，code={}, message={}"
                , realIp, uri, className, httpStatusCode, result.getBizCode(), getMessage(result));
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
        return ResponseEntity.status(httpStatusCode).body(result);
    }

    private boolean serverErrorFlag(HttpStatusCode statusCode) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value() <= statusCode.value();
    }

    private String getMessage(ResultVO<Object> result) {
        return ResultVO.message(result.getBizCode(), result.getBizMessageParams(), result.getBizMessage(), null);
    }
}
