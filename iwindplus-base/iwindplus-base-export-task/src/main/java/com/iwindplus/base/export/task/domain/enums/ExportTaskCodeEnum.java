/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.export.task.domain.enums;

import com.iwindplus.base.domain.exception.CommonException;
import lombok.Getter;

/**
 * 业务编码枚举.
 *
 * @author zengdegui
 * @since 2026/09/17
 */
@Getter
public enum ExportTaskCodeEnum implements CommonException {
    /**
     * 导出任务未配置Excel行模型类型.
     */
    EXPORT_ROW_CLASS_NOT_CONFIGURED("export_row_class_not_configured", "导出任务未配置Excel行模型类型"),

    /**
     * 导出任务未配置查询参数类型.
     */
    EXPORT_QUERY_CLASS_NOT_CONFIGURED("export_query_class_not_configured", "导出任务未配置查询参数类型"),

    /**
     * 导出任务查询参数解析失败.
     */
    EXPORT_QUERY_PARAM_PARSE_FAILED("export_query_param_parse_failed", "导出任务查询参数解析失败"),

    ;
    /**
     * 业务编码.
     */
    private final String bizCode;

    /**
     * 业务信息.
     */
    private final String bizMessage;

    /**
     * 构造方法.
     *
     * @param bizCode    业务编码
     * @param bizMessage 业务信息
     */
    ExportTaskCodeEnum(final String bizCode, final String bizMessage) {
        this.bizCode = bizCode;
        this.bizMessage = bizMessage;
    }
}
