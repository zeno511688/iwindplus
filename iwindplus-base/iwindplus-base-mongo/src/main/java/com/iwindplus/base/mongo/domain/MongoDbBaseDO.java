/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mongo.domain;

import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.OtherEditGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档数据库基础通用字段实体类.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "文档数据库基础通用字段实体类")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MongoDbBaseDO implements Serializable {

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
    @CreatedDate
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
    @CreatedBy
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
    @LastModifiedDate
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
    @LastModifiedBy
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
     * 乐观锁.
     */
    @Schema(description = "乐观锁")
    private Integer version;

    /**
     * 备注.
     */
    @Schema(description = "备注")
    private String remark;
}
