/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * binlog告警搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "binlog告警搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogAlertSearchDTO extends DbPageDTO {

    /**
     * 数据库名.
     */
    @Schema(description = "数据库名")
    @Length(max = 100, message = "{db.length}")
    private String db;

    /**
     * 表名.
     */
    @Schema(description = "表名")
    @Length(max = 100, message = "{table.length}")
    private String table;

    /**
     * 数据主键.
     */
    @Schema(description = "数据主键")
    private Long dataId;
}
