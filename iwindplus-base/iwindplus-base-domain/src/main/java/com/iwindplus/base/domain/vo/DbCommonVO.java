/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 数据库公共字段视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "数据库公共字段视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DbCommonVO implements Serializable {

    /**
     * 创建时间.
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    /**
     * 创建时间戳.
     */
    @Schema(description = "创建时间戳")
    private Long createdTimestamp;

    /**
     * 创建人.
     */
    @Schema(description = "创建人")
    private String createdBy;

    /**
     * 创建人主键.
     */
    @Schema(description = "创建人主键")
    private Long createdId;

    /**
     * 更新时间.
     */
    @Schema(description = "更新时间")
    private LocalDateTime modifiedTime;

    /**
     * 更新时间戳.
     */
    @Schema(description = "更新时间戳")
    private Long modifiedTimestamp;

    /**
     * 更新人.
     */
    @Schema(description = "更新人")
    private String modifiedBy;

    /**
     * 更新人主键.
     */
    @Schema(description = "更新人主键")
    private Long modifiedId;

    /**
     * 是否删除.
     */
    @Schema(description = "是否删除")
    private Integer deleted;

    /**
     * 备注.
     */
    @Schema(description = "备注")
    private String remark;
}
