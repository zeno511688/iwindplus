/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.feign.domain.enums;

/**
 * Feign 错误响应解析策略.
 *
 * @author zengdegui
 * @since 2026/8/25
 */
public enum FeignErrorResponseFormatEnum {

    /**
     * 按项目统一 ResultVO 解析.
     */
    RESULT_VO,

    /**
     * 按通用错误响应解析，保留 HTTP 错误信息.
     */
    GENERIC,

    /**
     * 不解析响应体，保留默认 Feign 异常.
     */
    NONE

}
