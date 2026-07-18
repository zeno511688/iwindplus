/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mybatis.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.OtherEditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 数据库基础通用字段实体类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "数据库基础通用字段实体类")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DbBaseDO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    @NotNull(message = "{id.notEmpty}", groups = {EditGroup.class, OtherEditGroup.class})
    @TableId
    private Long id;

    /**
     * 创建时间.
     */
    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 创建时间戳.
     */
    @Schema(description = "创建时间戳")
    @TableField(fill = FieldFill.INSERT)
    private Long createdTimestamp;

    /**
     * 创建人.
     */
    @Schema(description = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 创建人主键.
     */
    @Schema(description = "创建人主键")
    @TableField(fill = FieldFill.INSERT)
    private Long createdId;

    /**
     * 更新时间.
     */
    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifiedTime;

    /**
     * 更新时间戳.
     */
    @Schema(description = "更新时间戳")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long modifiedTimestamp;

    /**
     * 更新人.
     */
    @Schema(description = "更新人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String modifiedBy;

    /**
     * 更新人主键.
     */
    @Schema(description = "更新人主键")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long modifiedId;

    /**
     * 是否删除.
     */
    @Schema(description = "是否删除")
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 乐观锁.
     */
    @Schema(description = "乐观锁")
    @Version
    @TableField(fill = FieldFill.INSERT_UPDATE, update = "%s+1")
    private Integer version;

    /**
     * 备注.
     */
    @Schema(description = "备注")
    @Length(max = 255, message = "{remark.length}", groups = {SaveGroup.class, EditGroup.class, OtherEditGroup.class})
    private String remark;
}
