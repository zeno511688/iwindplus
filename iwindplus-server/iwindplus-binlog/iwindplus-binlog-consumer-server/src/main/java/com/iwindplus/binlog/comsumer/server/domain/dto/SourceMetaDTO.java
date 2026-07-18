/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 元数据传输对象.
 *
 * @author zengdegui
 * @since 2025/11/29 01:12
 */
@Schema(description = "元数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SourceMetaDTO implements Serializable {

    /**
     * 数据源id.
     */
    @JsonProperty("server_id")
    private Long serverId;

    /**
     * 数据库名.
     */
    @Schema(description = "数据库名")
    private String db;

    /**
     * 表名.
     */
    @Schema(description = "表名")
    private String table;

    /**
     * 处理时间（毫秒）.
     */
    @Schema(description = "处理时间（毫秒）")
    @JsonProperty("ts_ms")
    private Long tsMs;

    /**
     * 处理时间（微秒）.
     */
    @Schema(description = "处理时间（微秒）")
    @JsonProperty("ts_us")
    private Long tsUs;

    /**
     * 处理时间（纳秒）.
     */
    @Schema(description = "处理时间（纳秒）")
    @JsonProperty("ts_ns")
    private Long tsNs;

    /**
     * 文件名.
     */
    @Schema(description = "文件名")
    private String file;

    /**
     * 文件位置.
     */
    @Schema(description = "文件位置")
    private Long pos;

    /**
     * 全局事物唯一标识.
     */
    @Schema(description = "全局事物唯一标识")
    @JsonProperty("gtid")
    private String gtId;

    /**
     * 是否是快照数据.
     */
    @Schema(description = "是否是快照数据")
    private Boolean snapshot;

    /**
     * 版本.
     */
    @Schema(description = "版本")
    private String version;

    /**
     * 连接器.
     */
    @Schema(description = "连接器")
    private String connector;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 行号.
     */
    @Schema(description = "行号")
    private Long row;

    /**
     * 查询.
     */
    @Schema(description = "查询")
    private String query;
}
