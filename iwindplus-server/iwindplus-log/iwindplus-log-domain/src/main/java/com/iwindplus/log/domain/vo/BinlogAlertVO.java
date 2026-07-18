/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.vo;

import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.base.domain.vo.DbBaseTwoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * binlog告警详情视图对象.
 *
 * @author zengdegui
 * @since 2019/7/16
 */
@Schema(description = "binlog告警详情视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogAlertVO extends DbBaseTwoVO {

    /**
     * 处理时间（毫秒）.
     */
    @Schema(description = "处理时间（毫秒）")
    private Long tsMs;

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
     * 数据主键.
     */
    @Schema(description = "数据主键")
    private Long dataId;

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
     * 操作类型.
     */
    @Schema(description = "操作类型")
    private DbActionTypeEnum actionType;

    /**
     * 操作前数据.
     */
    @Schema(description = "操作前数据")
    private String before;

    /**
     * 操作后数据.
     */
    @Schema(description = "操作后数据")
    private String after;

    /**
     * 告警信息.
     */
    @Schema(description = "告警信息")
    private String message;
}
