/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.exception;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.iwindplus.base.domain.vo.ResultVO;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 自定义运行时异常【业务信息支持变量（示例："{0},{1}"】.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Getter
public class BizException extends RuntimeException implements CommonException {

    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务消息.
     */
    private final String bizMessage;

    /**
     * 业务信息参数.
     */
    private Object[] bizMessageParams;

    /**
     * 构造方法.
     *
     * @param httpStatus http状态码（必填）
     */
    public BizException(HttpStatus httpStatus) {
        this(httpStatus.name().toLowerCase(), httpStatus.getReasonPhrase(), null);
    }

    /**
     * 构造方法.
     *
     * @param commonException 异常（必填）
     */
    public BizException(CommonException commonException) {
        this(commonException, null, null);
    }

    /**
     * 构造方法.
     *
     * @param commonException 异常（必填）
     * @param bizMessage      业务信息（可选）
     */
    public BizException(CommonException commonException, String bizMessage) {
        this(commonException, bizMessage, null);
    }

    /**
     * 构造方法.
     *
     * @param commonException  异常（必填）
     * @param bizMessageParams 业务信息参数（可选）
     */
    public BizException(CommonException commonException, Object[] bizMessageParams) {
        this(commonException, null, bizMessageParams);
    }

    /**
     * 构造方法.
     *
     * @param commonException  异常（必填）
     * @param bizMessage       业务信息（可选）
     * @param bizMessageParams 业务信息参数（可选）
     */
    public BizException(CommonException commonException, String bizMessage, Object[] bizMessageParams) {
        this(commonException.getBizCode(),
            CharSequenceUtil.isNotBlank(bizMessage) ? bizMessage : commonException.getBizMessage(),
            ArrayUtil.isNotEmpty(bizMessageParams) ? bizMessageParams : commonException.getBizMessageParams());
    }

    /**
     * 构造方法.
     *
     * @param bizCode    业务编码（必填）
     * @param bizMessage 业务信息（必填）
     */
    public BizException(final String bizCode, final String bizMessage) {
        this(bizCode, bizMessage, null);
    }

    /**
     * 构造方法.
     *
     * @param bizCode          业务编码（必填）
     * @param bizMessage       业务信息（必填）
     * @param bizMessageParams 业务信息参数（可选）
     */
    public BizException(final String bizCode, final String bizMessage, final Object[] bizMessageParams) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
        this.bizMessageParams = bizMessageParams;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
            "{" +
            "bizCode='" + bizCode + '\'' +
            ", bizMessage='" + ResultVO.message(bizCode, bizMessageParams, bizMessage, null) +
            '}';
    }

    @Override
    public String getMessage() {
        return ResultVO.message(this.bizCode, this.bizMessageParams, this.bizMessage, null);
    }
}
