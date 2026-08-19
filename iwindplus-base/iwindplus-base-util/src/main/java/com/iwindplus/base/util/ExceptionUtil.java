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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
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
     * 异常处理器映射（保持插入顺序）.
     */
    private static final Map<String, BiFunction<Throwable, String, ResponseEntity<ResultVO<Object>>>> EXCEPTION_HANDLERS = new LinkedHashMap<>();

    static {
        // 参数校验相关异常（需要提取消息）
        registerHandler(ExceptionConstant.CONSTRAINT_VIOLATION_EXCEPTION, ExceptionUtil::handleConstraintViolation);
        registerHandler(ExceptionConstant.METHOD_ARGUMENT_NOT_VALID_EXCEPTION, ExceptionUtil::handleMethodArgumentNotValid);
        registerHandler(ExceptionConstant.BIND_EXCEPTION, ExceptionUtil::handleBindException);

        // 参数相关异常（需要提取参数名）
        registerHandler(ExceptionConstant.SERVER_WEB_INPUT_EXCEPTION, ExceptionUtil::handleServerWebInput);
        registerHandler(ExceptionConstant.MISSING_SERVLET_REQUEST_PARAMETER_EXCEPTION, ExceptionUtil::handleMissingServletRequestParameter);
        registerHandler(ExceptionConstant.METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION, ExceptionUtil::handleMethodArgumentTypeMismatch);

        // 简单异常（直接返回固定错误码）
        registerSimpleHandler(ExceptionConstant.MULTIPART_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.FILE_SIZE_LIMIT);
        registerSimpleHandler(ExceptionConstant.ILLEGAL_ARGUMENT_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.PARAM_ILLEGAL);
        registerSimpleHandler(ExceptionConstant.UNKNOWN_HOST_EXCEPTION, HttpStatus.SERVICE_UNAVAILABLE, BizCodeEnum.UNKNOWN_HOST);
        registerSimpleHandler(ExceptionConstant.SOCKET_EXCEPTION, HttpStatus.SERVICE_UNAVAILABLE, BizCodeEnum.SOCKET_ERROR);
        registerSimpleHandler(ExceptionConstant.SERVICE_UNAVAILABLE_EXCEPTION, HttpStatus.SERVICE_UNAVAILABLE, BizCodeEnum.SERVICE_UNAVAILABLE);
        registerSimpleHandler(ExceptionConstant.TIMEOUT_EXCEPTION, HttpStatus.REQUEST_TIMEOUT, BizCodeEnum.REQUEST_TIMEOUT);
        registerSimpleHandler(ExceptionConstant.DECODING_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.DECODING_ERROR);
        registerSimpleHandler(ExceptionConstant.FILE_NOT_FOUND_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.FILE_NOT_FOUND);
        registerSimpleHandler(ExceptionConstant.UNSUPPORTED_OPERATION_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.UNSUPPORTED_OPERATION);
        registerSimpleHandler(ExceptionConstant.HTTP_MESSAGE_NOT_READABLE_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.NOT_READABLE);
        registerSimpleHandler(ExceptionConstant.HTTP_MESSAGE_NOT_WRITABLE_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.NOT_WRITABLE);
        registerSimpleHandler(ExceptionConstant.NULL_POINTER_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.NULL_POINTER);
        registerSimpleHandler(ExceptionConstant.CONVERSION_NOT_SUPPORTED_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.CONVERSION_NOT_SUPPORTED);
        registerSimpleHandler(ExceptionConstant.MISSING_SERVLET_REQUEST_PART_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.MISSING_FILE);
        registerSimpleHandler(ExceptionConstant.MAX_UPLOAD_SIZE_EXCEEDED_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.FILE_TOO_BIG);
        registerSimpleHandler(ExceptionConstant.CLASS_CAST_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.CLASS_CAST_ERROR);
        registerSimpleHandler(ExceptionConstant.NUMBER_FORMAT_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.ONLY_SUPPORT_NUMBER);
        registerSimpleHandler(ExceptionConstant.SECURITY_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.SECURITY_ERROR);
        registerSimpleHandler(ExceptionConstant.BAD_SQL_GRAMMAR_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.BAD_SQL_GRAMMAR);
        registerSimpleHandler(ExceptionConstant.SQL_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.SQL_ERROR);
        registerSimpleHandler(ExceptionConstant.MYBATIS_SYSTEM_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.MYBATIS_ERROR);
        registerSimpleHandler(ExceptionConstant.DATA_INTEGRITY_VIOLATION_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.DATA_INTEGRITY_VIOLATION_ERROR);
        registerSimpleHandler(ExceptionConstant.TYPE_NOT_PRESENT_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.TYPE_NOT_PRESENT);
        registerSimpleHandler(ExceptionConstant.IO_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.IO_ERROR);
        registerSimpleHandler(ExceptionConstant.NO_SUCH_METHOD_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.NO_SUCH_METHOD);
        registerSimpleHandler(ExceptionConstant.INDEX_OUT_OF_BOUNDS_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.INDEX_OUT_OF_BOUNDS);
        registerSimpleHandler(ExceptionConstant.NO_SUCH_BEAN_DEFINITION_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.NO_SUCH_BEAN);
        registerSimpleHandler(ExceptionConstant.TYPE_MISMATCH_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.TYPE_MISMATCH);
        registerSimpleHandler(ExceptionConstant.STACK_OVERFLOW_ERROR, HttpStatus.BAD_REQUEST, BizCodeEnum.STACK_OVERFLOW);
        registerSimpleHandler(ExceptionConstant.ARITHMETIC_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.ARITHMETIC_ERROR);
        registerSimpleHandler(ExceptionConstant.MAIL_SEND_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.SEND_ERROR);
        registerSimpleHandler(ExceptionConstant.SERIALIZATION_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.SERIALIZE_ERROR);
        registerSimpleHandler(ExceptionConstant.JSON_PROCESSING_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.JSON_PROCESSING_ERROR);
        registerSimpleHandler(ExceptionConstant.JSON_MAPPING_EXCEPTION, HttpStatus.BAD_REQUEST, BizCodeEnum.JSON_MAPPING_ERROR);

        // 使用 HttpStatus 的异常
        registerHttpStatusHandler(ExceptionConstant.UNAUTHORIZED_EXCEPTION, HttpStatus.UNAUTHORIZED);
        registerHttpStatusHandler(ExceptionConstant.HTTP_REQUEST_METHOD_NOT_SUPPORTED_EXCEPTION, HttpStatus.METHOD_NOT_ALLOWED);
        registerHttpStatusHandler(ExceptionConstant.HTTP_MEDIA_TYPE_NOT_SUPPORTED_EXCEPTION, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        registerHttpStatusHandler(ExceptionConstant.HTTP_MEDIA_TYPE_NOT_ACCEPTABLE_EXCEPTION, HttpStatus.NOT_ACCEPTABLE);

        // 404 相关异常（多个异常类型对应同一处理）
        registerHandler(ExceptionConstant.NO_HANDLER_FOUND_EXCEPTION, ExceptionUtil::handleNotFound);
        registerHandler(ExceptionConstant.NO_RESOURCE_FOUND_EXCEPTION, ExceptionUtil::handleNotFound);
        registerHandler(ExceptionConstant.NOT_FOUND_EXCEPTION, ExceptionUtil::handleNotFound);
    }

    /**
     * 捕获异常信息（用于统一异常处理）.
     *
     * @param ex        异常
     * @param className 类名
     * @return ResponseEntity<ResultVO < Object>>
     */
    public static ResponseEntity<ResultVO<Object>> getException(Throwable ex, String className) {
        for (Map.Entry<String, BiFunction<Throwable, String, ResponseEntity<ResultVO<Object>>>> entry : EXCEPTION_HANDLERS.entrySet()) {
            if (CharSequenceUtil.contains(className, entry.getKey())) {
                return entry.getValue().apply(ex, className);
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResultVO.error(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * 注册异常处理器.
     *
     * @param exceptionKey 异常键（异常类型标识）
     * @param handler      异常处理器
     */
    private static void registerHandler(String exceptionKey, BiFunction<Throwable, String, ResponseEntity<ResultVO<Object>>> handler) {
        EXCEPTION_HANDLERS.put(exceptionKey, handler);
    }

    /**
     * 注册简单异常处理器（返回固定 BizCodeEnum）.
     *
     * @param exceptionKey 异常键（异常类型标识）
     * @param status       HTTP 状态码
     * @param bizCode      业务错误码枚举
     */
    private static void registerSimpleHandler(String exceptionKey, HttpStatus status, BizCodeEnum bizCode) {
        EXCEPTION_HANDLERS.put(exceptionKey, (ex, className) ->
            ResponseEntity.status(status).body(ResultVO.error(bizCode)));
    }

    /**
     * 注册使用 HttpStatus 的异常处理器.
     *
     * @param exceptionKey 异常键（异常类型标识）
     * @param status       HTTP 状态码
     */
    private static void registerHttpStatusHandler(String exceptionKey, HttpStatus status) {
        EXCEPTION_HANDLERS.put(exceptionKey, (ex, className) ->
            ResponseEntity.status(status).body(ResultVO.error(status)));
    }

    /**
     * 处理约束违反异常.
     *
     * @param ex        异常对象
     * @param className 类名
     * @return 响应实体
     */
    private static ResponseEntity<ResultVO<Object>> handleConstraintViolation(Throwable ex, String className) {
        ConstraintViolationException exs = (ConstraintViolationException) ex;
        String message = getMessage(exs);
        BizCodeEnum bizCodeEnum = BizCodeEnum.PARAM_CONSTRAINT_VIOLATION;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResultVO.buildSourceResult(bizCodeEnum.getBizCode(), message));
    }

    /**
     * 处理方法参数无效异常.
     *
     * @param ex        异常对象
     * @param className 类名
     * @return 响应实体
     */
    private static ResponseEntity<ResultVO<Object>> handleMethodArgumentNotValid(Throwable ex, String className) {
        MethodArgumentNotValidException exs = (MethodArgumentNotValidException) ex;
        String message = getMessage(exs.getBindingResult());
        BizCodeEnum bizCodeEnum = BizCodeEnum.PARAM_INVALID;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResultVO.buildSourceResult(bizCodeEnum.getBizCode(), message));
    }

    /**
     * 处理绑定异常.
     *
     * @param ex        异常对象
     * @param className 类名
     * @return 响应实体
     */
    private static ResponseEntity<ResultVO<Object>> handleBindException(Throwable ex, String className) {
        BindException exs = (BindException) ex;
        String message = getMessage(exs.getBindingResult());
        BizCodeEnum bizCodeEnum = BizCodeEnum.PARAM_BIND_ERROR;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResultVO.buildSourceResult(bizCodeEnum.getBizCode(), message));
    }

    /**
     * 处理 Web 输入异常.
     *
     * @param ex        异常对象
     * @param className 类名
     * @return 响应实体
     */
    private static ResponseEntity<ResultVO<Object>> handleServerWebInput(Throwable ex, String className) {
        ServerWebInputException item = (ServerWebInputException) ex;
        String parameterName = Optional.ofNullable(item)
            .map(ServerWebInputException::getMethodParameter)
            .map(MethodParameter::getParameterName)
            .orElse(null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResultVO.error(BizCodeEnum.PARAM_INPUT_ERROR, new Object[]{parameterName}));
    }

    /**
     * 处理缺少请求参数异常.
     *
     * @param ex        异常对象
     * @param className 类名
     * @return 响应实体
     */
    private static ResponseEntity<ResultVO<Object>> handleMissingServletRequestParameter(Throwable ex, String className) {
        MissingServletRequestParameterException item = (MissingServletRequestParameterException) ex;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResultVO.error(BizCodeEnum.PARAM_MISS, new Object[]{item.getParameterName()}));
    }

    /**
     * 处理参数类型不匹配异常.
     *
     * @param ex        异常对象
     * @param className 类名
     * @return 响应实体
     */
    private static ResponseEntity<ResultVO<Object>> handleMethodArgumentTypeMismatch(Throwable ex, String className) {
        MethodArgumentTypeMismatchException item = (MethodArgumentTypeMismatchException) ex;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResultVO.error(BizCodeEnum.PARAM_TYPE_MISMATCH, new Object[]{item.getName()}));
    }

    /**
     * 处理 404 异常.
     *
     * @param ex        异常对象
     * @param className 类名
     * @return 响应实体
     */
    private static ResponseEntity<ResultVO<Object>> handleNotFound(Throwable ex, String className) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResultVO.error(HttpStatus.NOT_FOUND));
    }

    /**
     * 从约束违反异常中提取错误消息.
     *
     * @param exs 约束违反异常
     * @return 错误消息（多个消息用分号分隔）
     */
    private static String getMessage(ConstraintViolationException exs) {
        Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
        if (CollUtil.isNotEmpty(violations)) {
            return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(SymbolConstant.SEMICOLON));
        }
        return null;
    }

    /**
     * 从绑定结果中提取错误消息.
     *
     * @param exs 绑定结果
     * @return 错误消息（多个消息用分号分隔）
     */
    private static String getMessage(BindingResult exs) {
        List<ObjectError> allErrors = exs.getAllErrors();
        if (CollUtil.isNotEmpty(allErrors)) {
            return allErrors.stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(SymbolConstant.SEMICOLON));
        }
        return null;
    }
}
