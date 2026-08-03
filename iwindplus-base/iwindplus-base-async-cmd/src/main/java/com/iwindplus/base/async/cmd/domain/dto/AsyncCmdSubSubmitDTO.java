/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.dto;

import com.iwindplus.base.async.cmd.support.AsyncCmdSubTaskHandler;
import com.iwindplus.base.util.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 异步命令子任务提交数据传输对象.
 *
 * @author zengdegui
 * @since 2025/12/28 00:22
 */
@Schema(description = "异步命令子任务提交数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdSubSubmitDTO implements Serializable {

    /**
     * 业务类型（必填）.
     */
    @Schema(description = "业务类型，例如 ORDER、USER")
    private String bizType;

    /**
     * 排序号（必填）
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 内容（必填）.
     */
    @Schema(description = "内容")
    private Map<String, Object> content;

    /**
     * 执行器类（必填）.
     */
    @Schema(description = "执行器类")
    private Class<? extends AsyncCmdSubTaskHandler> executorClass;

    /**
     * 备注（可选）.
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 设置数据.
     *
     * @param data 数据
     * @param <T>  泛型
     */
    public <T> void setData(T data) {
        this.content = JacksonUtil.parseMap(JacksonUtil.toJsonStr(data));
    }

    /**
     * 获取数据并转换为指定类型.
     *
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return T
     */
    public <T> T getData(Class<T> clazz) {
        if (content == null) {
            return null;
        }
        // 使用 JSON 序列化/反序列化转换
        String json = JacksonUtil.toJsonStr(content);
        return JacksonUtil.parseObject(json, clazz);
    }
}
