/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.dto;

import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.OtherEditGroup;
import com.iwindplus.base.domain.validation.OtherSaveGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 数据库公共字段数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "数据库公共字段数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DbCommonDTO implements Serializable {

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
    @Length(max = 100, message = "{createdBy.length}", groups = {SaveGroup.class, EditGroup.class, OtherSaveGroup.class, OtherEditGroup.class})
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
    @Length(max = 100, message = "{modifiedBy.length}", groups = {SaveGroup.class, EditGroup.class, OtherSaveGroup.class, OtherEditGroup.class})
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
    @Length(max = 255, message = "{remark.length}", groups = {SaveGroup.class, EditGroup.class, OtherEditGroup.class})
    private String remark;
}
