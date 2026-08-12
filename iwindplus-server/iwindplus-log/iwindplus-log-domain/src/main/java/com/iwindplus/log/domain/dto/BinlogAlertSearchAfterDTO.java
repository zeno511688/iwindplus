/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * binlog告警搜索数据传输对象(search_after深分页).
 *
 * @author zengdegui
 * @since 2024/4/10
 */
@Schema(description = "binlog告警搜索数据传输对象(search_after深分页)")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogAlertSearchAfterDTO implements Serializable {

    /**
     * 每页显示条数.
     */
    @Schema(description = "每页显示条数")
    private Integer size;

    /**
     * 下一页游标.
     */
    @Schema(description = "下一页游标")
    private List<Object> searchAfter;

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
