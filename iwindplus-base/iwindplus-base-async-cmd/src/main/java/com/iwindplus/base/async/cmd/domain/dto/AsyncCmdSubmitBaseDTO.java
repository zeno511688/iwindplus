/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import com.iwindplus.base.util.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令提交基础数据传输对象.
 *
 * @author zengdegui
 * @since 2025/12/28 00:22
 */
@Schema(description = "异步命令提交基础数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubmitBaseDTO implements Serializable {

    /**
     * 业务名称（必填）.
     */
    @Schema(description = "业务名称")
    private String bizName;

    /**
     * 业务key（必填），例如 ORDER.
     */
    @Schema(description = "业务key，例如 ORDER")
    private String bizKey;

    /**
     * 业务类型（必填），例如 ORDER、USER.
     */
    @Schema(description = "业务类型，例如 ORDER_CREATE")
    private String bizType;

    /**
     * 参数（可选）.
     */
    @Schema(description = "参数")
    private Map<String, Object> param;

    /**
     * 业务流水号（可选）.
     */
    @Schema(description = "业务流水号")
    private String bizNumber;

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
     * 备注（可选）.
     */
    @Schema(description = "备注")
    private String remark;

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
