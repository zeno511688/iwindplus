/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.iwindplus.base.domain.constant.CommonConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.enums.BizCodeEnum;
import com.iwindplus.base.domain.vo.ResultVO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebInputException;

/**
 * 异常工具类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Slf4j
public class ExceptionUtil {

    private ExceptionUtil() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 捕获异常信息（用于统一异常处理）.
     *
     * @param ex        异常
     * @param className 类名
     * @return ResponseEntity<ResultVO < Object>>
     */
    public static ResponseEntity<ResultVO<Object>> getException(Throwable ex, String className) {
        if (CharSequenceUtil.contains(className, ExceptionConstant.CONSTRAINT_VIOLATION_EXCEPTION)) {
            final ConstraintViolationException exs = (ConstraintViolationException) ex;
            final String message = ExceptionUtil.getMessage(exs);
            final BizCodeEnum bizCodeEnum = BizCodeEnum.PARAM_CONSTRAINT_VIOLATION;
            final String bizCode = bizCodeEnum.getBizCode();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.buildSourceResult(bizCode, message));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.METHOD_ARGUMENT_NOT_VALID_EXCEPTION)) {
            final MethodArgumentNotValidException exs = (MethodArgumentNotValidException) ex;
            final String message = ExceptionUtil.getMessage(exs.getBindingResult());
            final BizCodeEnum bizCodeEnum = BizCodeEnum.PARAM_INVALID;
            final String bizCode = bizCodeEnum.getBizCode();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.buildSourceResult(bizCode, message));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.BIND_EXCEPTION)) {
            final BindException exs = (BindException) ex;
            final String message = ExceptionUtil.getMessage(exs.getBindingResult());
            final BizCodeEnum bizCodeEnum = BizCodeEnum.PARAM_BIND_ERROR;
            final String bizCode = bizCodeEnum.getBizCode();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.buildSourceResult(bizCode, message));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.MULTIPART_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.FILE_SIZE_LIMIT));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.ILLEGAL_ARGUMENT_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.PARAM_ILLEGAL));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.SERVER_WEB_INPUT_EXCEPTION)) {
            final ServerWebInputException item = (ServerWebInputException) ex;
            String parameterName = Optional.ofNullable(item).map(ServerWebInputException::getMethodParameter)
                .map(MethodParameter::getParameterName).orElse(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.PARAM_INPUT_ERROR, new Object[]{parameterName}));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION)) {
            final MissingServletRequestParameterException item = (MissingServletRequestParameterException) ex;
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.PARAM_MISS, new Object[]{item.getParameterName()}));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION)) {
            final MethodArgumentTypeMismatchException item = (MethodArgumentTypeMismatchException) ex;
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.PARAM_TYPE_MISMATCH, new Object[]{item.getName()}));
        }

        return ExceptionUtil.getExceptionOne(className);
    }

    private static ResponseEntity<ResultVO<Object>> getExceptionOne(String className) {
        if (CharSequenceUtil.contains(className, ExceptionConstant.UNKNOWN_HOST_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ResultVO.error(BizCodeEnum.UNKNOWN_HOST));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.SOCKET_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ResultVO.error(BizCodeEnum.SOCKET_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.SERVICE_UNAVAILABLE_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ResultVO.error(BizCodeEnum.SERVICE_UNAVAILABLE));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.TIMEOUT_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(ResultVO.error(BizCodeEnum.REQUEST_TIMEOUT));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.DECODING_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.DECODING_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.UNAUTHORIZED_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResultVO.error(HttpStatus.UNAUTHORIZED));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.FILE_NOT_FOUND_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.FILE_NOT_FOUND));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.NO_HANDLER_FOUND_EXCEPTION)
            || CharSequenceUtil.contains(className, ExceptionConstant.NO_RESOURCE_FOUND_EXCEPTION)
            || CharSequenceUtil.contains(className, ExceptionConstant.NOT_FOUND_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResultVO.error(HttpStatus.NOT_FOUND));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ResultVO.error(HttpStatus.METHOD_NOT_ALLOWED));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.HTTP_MEDIA_TYPE_NOT_SUPPORTED_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ResultVO.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ResultVO.error(HttpStatus.NOT_ACCEPTABLE));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.UNSUPPORTED_OPERATION_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.UNSUPPORTED_OPERATION));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.HTTP_MESSAGE_NOT_READABLE_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.NOT_READABLE));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.NOT_WRITABLE));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.NULL_POINTER_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.NULL_POINTER));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.CONVERSION_NOT_SUPPORTED_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.CONVERSION_NOT_SUPPORTED));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.MISSING_SERVLET_REQUEST_PART_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.MISSING_FILE));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.MAX_UPLOAD_SIZE_EXCEEDED_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.FILE_TOO_BIG));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.CLASS_CAST_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.CLASS_CAST_ERROR));
        }

        return ExceptionUtil.getExceptionTwo(className);
    }

    private static ResponseEntity<ResultVO<Object>> getExceptionTwo(String className) {
        if (CharSequenceUtil.contains(className, ExceptionConstant.NUMBER_FORMAT_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.ONLY_SUPPORT_NUMBER));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.SECURITY_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.SECURITY_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.BAD_SQL_GRAMMAR_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.BAD_SQL_GRAMMAR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.SQL_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.SQL_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.MYBATIS_SYSTEM_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.MYBATIS_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.DATA_INTEGRITY_VIOLATION_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.DATA_INTEGRITY_VIOLATION_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.TYPE_NOT_PRESENT_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.TYPE_NOT_PRESENT));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.IO_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.IO_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.NO_SUCH_METHOD_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.NO_SUCH_METHOD));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.INDEX_OUT_OF_BOUNDS_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.INDEX_OUT_OF_BOUNDS));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.NO_SUCH_BEAN_DEFINITION_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.NO_SUCH_BEAN));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.TYPE_MISMATCH_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.TYPE_MISMATCH));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.STACK_OVERFLOW_ERROR)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.STACK_OVERFLOW));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.ARITHMETIC_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.ARITHMETIC_ERROR));
        }

        return ExceptionUtil.getExceptionThree(className);
    }

    private static ResponseEntity<ResultVO<Object>> getExceptionThree(String className) {
        if (CharSequenceUtil.contains(className, ExceptionConstant.MAIL_SEND_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.SEND_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.SERIALIZATION_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.SERIALIZE_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.JSON_PROCESSING_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.JSON_PROCESSING_ERROR));
        } else if (CharSequenceUtil.contains(className, ExceptionConstant.JSON_MAPPING_EXCEPTION)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResultVO.error(BizCodeEnum.JSON_MAPPING_ERROR));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResultVO.error(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private static String getMessage(ConstraintViolationException exs) {
        final Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
        if (CollUtil.isNotEmpty(violations)) {
            return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(SymbolConstant.SEMICOLON));
        }
        return null;
    }

    private static String getMessage(BindingResult exs) {
        List<ObjectError> allErrors = exs.getAllErrors();
        if (CollUtil.isNotEmpty(allErrors)) {
            return allErrors.stream().map(s -> s.getDefaultMessage()).collect(Collectors.joining(SymbolConstant.SEMICOLON));
        }
        return null;
    }
}
