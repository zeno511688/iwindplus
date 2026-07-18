/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * 公共的异常接口.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
public interface CommonException {

    /**
     * 业务编码.
     *
     * @return String
     */
    default String getBizCode() {
        return null;
    }

    /**
     * 业务信息（变量示例："{0},{1}"）.
     *
     * @return String
     */
    default String getBizMessage() {
        return null;
    }

    /**
     * 业务信息参数.
     *
     * @return Object[]
     */
    default Object[] getBizMessageParams() {
        return null;
    }

    /**
     * 构造器.
     *
     * @param httpStatus http状态码
     * @return CommonException
     */
    static CommonException build(HttpStatus httpStatus) {
        return new CommonException() {

            @Override
            public String getBizCode() {
                return httpStatus.name().toLowerCase();
            }

            @Override
            public String getBizMessage() {
                return httpStatus.getReasonPhrase();
            }

            @Override
            public Object[] getBizMessageParams() {
                return null;
            }
        };
    }

    /**
     * 构造器.
     *
     * @param bizCode          业务编码
     * @param bizMessage       业务信息
     * @param bizMessageParams 业务信息参数
     * @return CommonException
     */
    static CommonException build(String bizCode, String bizMessage, Object[] bizMessageParams) {
        return new CommonException() {

            @Override
            public String getBizCode() {
                return bizCode;
            }

            @Override
            public String getBizMessage() {
                return bizMessage;
            }

            @Override
            public Object[] getBizMessageParams() {
                return bizMessageParams;
            }
        };
    }
}
