/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import com.iwindplus.base.async.cmd.support.AsyncCmdTaskHandler;
import com.iwindplus.base.util.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令提交数据传输对象.
 *
 * @author zengdegui
 * @since 2025/12/28 00:22
 */
@Schema(description = "异步命令提交数据传输对象")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubmitDTO extends AsyncCmdSubmitBaseDTO {

    /**
     * 参数（可选）.
     */
    @Schema(description = "参数")
    private Map<String, Object> param;

    /**
     * 执行器类（必填）.
     */
    @Schema(description = "执行器类")
    private Class<? extends AsyncCmdTaskHandler> executorClass;

    /**
     * 备注（可选）.
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 是否需要回调（可选）.
     */
    @Schema(description = "是否需要回调")
    private Boolean needCallback;

    /**
     * 是否需要显示（可选，查进度时用）.
     */
    @Schema(description = "是否需要显示")
    private Boolean needDisplay;

    /**
     * 设置参数.
     *
     * @param data 数据
     * @param <T>  泛型
     */
    public <T> void setParam(T data) {
        this.param = JacksonUtil.parseMap(JacksonUtil.toJsonStr(data));
    }

    /**
     * 获取参数并转换为指定类型.
     *
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return T
     */
    public <T> T getParam(Class<T> clazz) {
        if (param == null) {
            return null;
        }
        // 使用 JSON 序列化/反序列化转换
        String json = JacksonUtil.toJsonStr(param);
        return JacksonUtil.parseObject(json, clazz);
    }
}
