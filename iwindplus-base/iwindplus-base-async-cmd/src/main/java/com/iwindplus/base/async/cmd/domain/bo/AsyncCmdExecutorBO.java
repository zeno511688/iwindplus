/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.async.cmd.domain.bo;

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
 * 异步命令执行数据传输对象.
 *
 * @author zengdegui
 * @since 2025/12/28 00:22
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AsyncCmdExecutorBO extends AsyncCmdExecutorBaseBO {

    /**
     * 内容（必填）.
     */
    @Schema(description = "内容")
    private Map<String, Object> content;

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
