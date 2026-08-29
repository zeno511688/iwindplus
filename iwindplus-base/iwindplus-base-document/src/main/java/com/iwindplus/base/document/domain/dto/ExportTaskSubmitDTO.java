/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.document.domain.dto;

import com.iwindplus.base.document.support.ExportTaskHandler;
import com.iwindplus.base.util.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 导出任务提交数据传输对象.
 *
 * @author zengdegui
 * @since 2026/08/29
 */
@Schema(description = "导出任务提交数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExportTaskSubmitDTO implements Serializable {

    /**
     * 执行器类（必填）.
     */
    @Schema(description = "执行器类")
    private Class<? extends ExportTaskHandler> executorClass;

    /**
     * 文件名（必填）.
     */
    @Schema(description = "文件名")
    private String fileName;

    /**
     * 参数（必填）.
     */
    @Schema(description = "参数")
    private Map<String, Object> queryParam;

    /**
     * 业务流水号（可选）.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

    /**
     * 备注（可选）.
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 扩展对象.
     */
    @Schema(description = "扩展对象")
    private ExportTaskExtDTO ext;

    /**
     * 设置参数.
     *
     * @param data 数据
     * @param <T>  泛型
     */
    public <T> void setQueryParam(T data) {
        this.queryParam = JacksonUtil.parseMap(JacksonUtil.toJsonStr(data));
    }

    /**
     * 获取参数并转换为指定类型.
     *
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return T
     */
    public <T> T getQueryParam(Class<T> clazz) {
        if (queryParam == null) {
            return null;
        }
        // 使用 JSON 序列化/反序列化转换
        String json = JacksonUtil.toJsonStr(queryParam);
        return JacksonUtil.parseObject(json, clazz);
    }
}
