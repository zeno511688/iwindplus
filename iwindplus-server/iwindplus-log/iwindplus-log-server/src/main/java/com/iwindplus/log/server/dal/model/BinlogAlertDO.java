/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.log.server.dal.model;

import com.iwindplus.base.es.domain.EsDbBaseDO;
import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * binlog告警.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "binlog告警对象")
@Document(indexName = "binlog_alert")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogAlertDO extends EsDbBaseDO {

    /**
     * 处理时间（毫秒）.
     */
    @Schema(description = "处理时间（毫秒）")
    @Field(type = FieldType.Long)
    private Long tsMs;

    /**
     * 数据库名.
     */
    @Schema(description = "数据库名")
    @Field(type = FieldType.Keyword)
    private String db;

    /**
     * 表名.
     */
    @Schema(description = "表名")
    @Field(type = FieldType.Keyword)
    private String table;

    /**
     * 数据主键.
     */
    @Schema(description = "数据主键")
    @Field(type = FieldType.Long)
    private Long dataId;

    /**
     * 文件名.
     */
    @Schema(description = "文件名")
    @Field(type = FieldType.Text, index = false)
    private String file;

    /**
     * 文件位置.
     */
    @Schema(description = "文件位置")
    @Field(type = FieldType.Long)
    private Long pos;

    /**
     * 操作类型.
     */
    @Schema(description = "操作类型")
    @Field(type = FieldType.Text, index = false)
    private DbActionTypeEnum actionType;

    /**
     * 操作前数据.
     */
    @Schema(description = "操作前数据")
    @Field(type = FieldType.Text, index = false)
    private String before;

    /**
     * 操作后数据.
     */
    @Schema(description = "操作后数据")
    @Field(type = FieldType.Text, index = false)
    private String after;

    /**
     * 告警信息.
     */
    @Schema(description = "告警信息")
    @Field(type = FieldType.Text, index = false)
    private String message;
}
