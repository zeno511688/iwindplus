/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwindplus.base.domain.constant.CommonConstant.ExceptionConstant;
import com.iwindplus.base.domain.constant.CommonConstant.HeaderConstant;
import com.iwindplus.base.domain.constant.CommonConstant.ResponseConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SymbolConstant;
import com.iwindplus.base.domain.constant.CommonConstant.SystemConstant;
import com.iwindplus.base.domain.exception.BizException;
import com.iwindplus.base.domain.exception.CommonException;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

/**
 * 结果视图对象.
 *
 * @param <T> 泛型
 * @author zengdegui
 * @since 2020/10/31
 */
@Slf4j
@Schema(description = "结果视图对象")
@Data
@SuperBuilder
@AllArgsConstructor
public class ResultVO<T> implements Serializable {

    /**
     * 业务编码.
     */
    @Schema(description = "业务编码")
    @JsonProperty(ResponseConstant.BIZ_CODE)
    private String bizCode;

    /**
     * 业务信息.
     */
    @Schema(description = "业务信息")
    @JsonProperty(ResponseConstant.BIZ_MESSAGE)
    private String bizMessage;

    /**
     * 业务信息参数.
     */
    @Schema(description = "业务信息参数", hidden = true)
    @JsonProperty(value = ResponseConstant.BIZ_MESSAGE_PARAMS, access = JsonProperty.Access.WRITE_ONLY)
    private Object[] bizMessageParams;

    /**
     * 业务数据.
     */
    @Schema(description = "业务数据")
    @JsonProperty(ResponseConstant.BIZ_DATA)
    private T bizData;

    /**
     * 业务时间.
     */
    @Schema(description = "业务时间")
    @JsonProperty(ResponseConstant.BIZ_TIME)
    private LocalDateTime bizTime;

    /**
     * 业务时间戳.
     */
    @Schema(description = "业务时间戳")
    @JsonProperty(ResponseConstant.BIZ_TIMESTAMP)
    private Long bizTimestamp;

    /**
     * 业务跟踪唯一标识.
     */
    @Schema(description = "业务跟踪唯一标识")
    @JsonProperty(ResponseConstant.BIZ_TRACE_ID)
    private String bizTraceId;

    /**
     * 构造方法.
     */
    public ResultVO() {
        this.bizCode = HttpStatus.OK.name().toLowerCase();
        this.bizMessage = HttpStatus.OK.getReasonPhrase();
        this.bizTime = LocalDateTime.now();
        this.bizTimestamp = System.currentTimeMillis();
        this.bizTraceId = MDC.get(HeaderConstant.TRACE_ID);
    }

    /**
     * 成功.
     *
     * @param <T> 泛型
     * @return ResultVO <T>
     */
    public static <T> ResultVO<T> success() {
        return ResultVO.success(null);
    }

    /**
     * 成功.
     *
     * @param bizData 业务数据
     * @param <T>     泛型
     * @return ResultVO <T>
     */
    public static <T> ResultVO<T> success(T bizData) {
        HttpStatus httpStatus = HttpStatus.OK;
        return ResultVO.buildResult(httpStatus.name().toLowerCase(), httpStatus.getReasonPhrase(), null, bizData);
    }

    /**
     * 失败.
     *
     * @param <T> 泛型
     * @return ResultVO<T>
     */
    public static <T> ResultVO<T> error() {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResultVO.error(httpStatus);
    }

    /**
     * 失败.
     *
     * @param httpStatus http状态码
     * @param <T>        泛型
     * @return ResultVO<T>
     */
    public static <T> ResultVO<T> error(HttpStatus httpStatus) {
        return ResultVO.buildResult(httpStatus.name().toLowerCase(), httpStatus.getReasonPhrase(), null);
    }

    /**
     * 失败.
     *
     * @param commonException 异常
     * @param <T>             泛型
     * @return ResultVO<T>
     */
    public static <T> ResultVO<T> error(CommonException commonException) {
        return ResultVO.buildResult(commonException.getBizCode(),
            commonException.getBizMessage(), commonException.getBizMessageParams());
    }

    /**
     * 失败.
     *
     * @param commonException  异常
     * @param bizMessageParams 业务信息参数
     * @param <T>              泛型
     * @return ResultVO<T>
     */
    public static <T> ResultVO<T> error(CommonException commonException, Object[] bizMessageParams) {
        return ResultVO.buildResult(commonException.getBizCode(), commonException.getBizMessage(),
            ArrayUtil.isNotEmpty(bizMessageParams) ? bizMessageParams : commonException.getBizMessageParams());
    }

    /**
     * 构造响应结果（消息原样输出）.
     *
     * @param bizCode    业务编码
     * @param bizMessage 业务信息
     * @param <T>        泛型
     * @return ResultVO<T>
     */
    public static <T> ResultVO<T> buildSourceResult(String bizCode, String bizMessage) {
        final ResultVO<T> result = new ResultVO<>();
        result.bizCode = bizCode;
        result.bizMessage = bizMessage;
        return result;
    }

    /**
     * 构造响应结果（消息国际化）.
     *
     * @param bizCode    业务编码
     * @param bizMessage 业务信息
     * @param <T>        泛型
     * @return ResultVO<T>
     */
    public static <T> ResultVO<T> buildResult(String bizCode, String bizMessage) {
        return ResultVO.buildResult(bizCode, bizMessage, null);
    }

    /**
     * 构造响应结果（消息国际化）.
     *
     * @param bizCode          业务编码
     * @param bizMessage       业务信息
     * @param bizMessageParams 业务信息参数
     * @param <T>              泛型
     * @return ResultVO<T>
     */
    public static <T> ResultVO<T> buildResult(String bizCode, String bizMessage, Object[] bizMessageParams) {
        return ResultVO.buildResult(bizCode, bizMessage, bizMessageParams, null);
    }

    /**
     * 构造响应结果（消息国际化）.
     *
     * @param bizCode          业务编码
     * @param bizMessage       业务信息
     * @param bizMessageParams 业务信息参数
     * @param bizData          业务数据
     * @param <T>              泛型
     * @return ResultVO<T>
     */
    public static <T> ResultVO<T> buildResult(String bizCode, String bizMessage, Object[] bizMessageParams, T bizData) {
        return ResultVO.buildResult(bizCode, bizMessage, bizMessageParams, bizData, null);
    }

    /**
     * 构造响应结果（消息国际化）.
     *
     * @param bizCode          业务编码
     * @param bizMessage       业务信息
     * @param bizMessageParams 业务信息参数
     * @param bizData          业务数据
     * @param locale           语言
     * @param <T>              泛型
     * @return ResultVO<T>
     */
    public static <T> ResultVO<T> buildResult(String bizCode, String bizMessage,
        Object[] bizMessageParams, T bizData, Locale locale) {
        final ResultVO<T> result = new ResultVO<>();
        result.bizCode = bizCode;
        result.bizMessage = ResultVO.message(bizCode, bizMessageParams, bizMessage, locale);
        result.bizData = bizData;
        result.bizMessageParams = bizMessageParams;
        return result;
    }

    /**
     * 判断是否成功.
     */
    public Boolean bizSuccess() {
        return HttpStatus.OK.name().toLowerCase().equals(this.bizCode);
    }

    /**
     * 判断是否错误.
     *
     * @return Boolean
     */
    public Boolean bizError() {
        return Boolean.FALSE.equals(this.bizSuccess());
    }

    /**
     * 错误异常抛出.
     */
    public void errorThrow() {
        final BizException bizException = this.errorException();
        if (null == bizException) {
            return;
        }

        throw bizException;
    }

    /**
     * 返回错误异常.
     *
     * @return BizException
     */
    public BizException errorException() {
        if (this.bizSuccess()) {
            return null;
        }

        return new BizException(this.bizCode, this.bizMessage, this.bizMessageParams);
    }

    /**
     * 根据消息键和参数获取国际化消息.
     *
     * @param code    编码
     * @param args    参数
     * @param message 消息
     * @param locale  语言
     * @return String
     */
    public static String message(String code, Object[] args, String message, Locale locale) {
        MessageSource messageSource = MessageSourceHolder.MESSAGE_SOURCE;
        if (null == messageSource) {
            return ResultVO.replaceStr(message, args);
        }

        locale = ResultVO.getLocale(locale);

        String msg;
        try {
            msg = messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException ex) {
            log.warn(SystemConstant.NO_I18N_MESSAGE + ", code={} for locale={}", code, locale);
            msg = ResultVO.replaceStr(message, args);
        }
        return msg;
    }

    /**
     * 替换字符串中变量.
     *
     * @param message 消息（变量示例："{0},{1}"）
     * @param args    参数
     * @return String
     */
    public static String replaceStr(String message, Object[] args) {
        if (CharSequenceUtil.isBlank(message) || ArrayUtil.isEmpty(args)) {
            return message;
        }

        try {
            return MessageFormat.format(message, args);
        } catch (Exception ex) {
            log.error(ExceptionConstant.EXCEPTION, ex);
        }
        return message;
    }

    /**
     * 获取国际化语言.
     *
     * @param locale 语言
     * @return Locale
     */
    public static Locale getLocale(Locale locale) {
        if (Objects.nonNull(locale)) {
            return locale;
        }
        String language = MDC.get(HttpHeaders.ACCEPT_LANGUAGE);
        if (CharSequenceUtil.isNotBlank(language) && CharSequenceUtil.contains(language, SymbolConstant.HORIZONTAL_LINE)) {
            locale = Locale.forLanguageTag(language);
        } else {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * 统一处理 ResultVO<T> 响应.
     *
     * @param result 响应
     * @param <T>    泛型
     * @return <T> Mono<T>
     */
    public static <T> Mono<T> unwrap(ResultVO<T> result) {
        return result.bizError()
            ? Mono.error(result.errorException())
            : Mono.justOrEmpty(result.getBizData());
    }

    /**
     * 静态内部类.
     */
    private static class MessageSourceHolder {

        /**
         * MessageSource.
         */
        static final MessageSource MESSAGE_SOURCE = SpringUtil.getBean(MessageSource.class);
    }
}
