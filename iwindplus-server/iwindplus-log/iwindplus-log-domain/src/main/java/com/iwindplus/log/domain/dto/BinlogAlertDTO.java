/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import com.iwindplus.base.domain.dto.DbBaseTwoDTO;
import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * binlog告警.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "binlog告警对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogAlertDTO extends DbBaseTwoDTO {

    /**
     * 处理时间（毫秒）.
     */
    @Schema(description = "处理时间（毫秒）")
    private Long tsMs;

    /**
     * 数据库名.
     */
    @Schema(description = "数据库名")
    @NotBlank(message = "{db.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{db.length}", groups = {SaveGroup.class, EditGroup.class})
    private String db;

    /**
     * 表名.
     */
    @Schema(description = "表名")
    @NotBlank(message = "{table.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{table.length}", groups = {SaveGroup.class, EditGroup.class})
    private String table;

    /**
     * 数据主键.
     */
    @Schema(description = "数据主键")
    @NotNull(message = "{dataId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
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
