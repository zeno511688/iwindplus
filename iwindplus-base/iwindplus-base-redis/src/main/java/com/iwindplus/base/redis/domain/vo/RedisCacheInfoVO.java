/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.redis.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * redis 缓存监控信息视图对象.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "redis 缓存监控信息视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RedisCacheInfoVO implements Serializable {

    /**
     * 监控指标信息.
     */
    @Schema(description = "监控指标信息")
    private Properties info;

    /**
     * 库大小.
     */
    @Schema(description = "库大小")
    private Long dbSize;

    /**
     * 命令统计.
     */
    @Schema(description = "命令统计")
    private List<Map<String, String>> commandStats;
}
