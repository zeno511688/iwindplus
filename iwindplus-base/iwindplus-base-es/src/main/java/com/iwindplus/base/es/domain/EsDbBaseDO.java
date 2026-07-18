/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.es.domain;

import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.OtherEditGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * es数据库基础通用字段实体类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "es数据库基础通用字段实体类")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EsDbBaseDO implements Serializable {

    /**
     * 主键.
     */
    @Schema(description = "主键")
    @NotBlank(message = "{id.notEmpty}", groups = {EditGroup.class, OtherEditGroup.class})
    @Id
    private String id;

    /**
     * 创建时间.
     */
    @Schema(description = "创建时间")
    @Field(
        type = FieldType.Date,
        format = {},
        pattern = "yyyy-MM-dd HH:mm:ss",
        index = false
    )
    private LocalDateTime createdTime;

    /**
     * 创建时间戳.
     */
    @Schema(description = "创建时间戳")
    @Field(type = FieldType.Long)
    private Long createdTimestamp;

    /**
     * 创建人.
     */
    @Schema(description = "创建人")
    @Field(type = FieldType.Keyword, index = false)
    private String createdBy;

    /**
     * 创建人主键.
     */
    @Schema(description = "创建人主键")
    @Field(type = FieldType.Long, index = false)
    private Long createdId;

    /**
     * 更新时间.
     */
    @Schema(description = "更新时间")
    @Field(
        type = FieldType.Date,
        format = {},
        pattern = "yyyy-MM-dd HH:mm:ss",
        index = false
    )
    private LocalDateTime modifiedTime;

    /**
     * 更新时间戳.
     */
    @Schema(description = "更新时间戳")
    @Field(type = FieldType.Long)
    private Long modifiedTimestamp;

    /**
     * 更新人.
     */
    @Schema(description = "更新人")
    @Field(type = FieldType.Keyword, index = false)
    private String modifiedBy;

    /**
     * 更新人主键.
     */
    @Schema(description = "更新人主键")
    @Field(type = FieldType.Long, index = false)
    private Long modifiedId;

    /**
     * 是否删除.
     */
    @Schema(description = "是否删除")
    @Field(type = FieldType.Integer, index = false)
    private Integer deleted;

    /**
     * 乐观锁.
     */
    @Schema(description = "乐观锁")
    @Field(type = FieldType.Integer, index = false)
    private Integer version;

    /**
     * 备注.
     */
    @Schema(description = "备注")
    @Field(type = FieldType.Text, index = false)
    private String remark;
}
