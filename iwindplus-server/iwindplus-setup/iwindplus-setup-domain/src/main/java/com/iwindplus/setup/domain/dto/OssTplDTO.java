/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 对象存储模板数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/27
 */
@Schema(description = "对象存储模板数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OssTplDTO extends DbVersionBaseDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    @NotBlank(message = "{templateName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{templateName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 空间名.
     */
    @Schema(description = "空间名")
    @NotBlank(message = "{bucketName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{bucketName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bucketName;

    /**
     * 自定义访问域名.
     */
    @Schema(description = "自定义访问域名")
    @Length(max = 100, message = "{accessDomain.length}", groups = {SaveGroup.class, EditGroup.class})
    private String accessDomain;

    /**
     * 分片上传，分片大小（单位：兆）.
     */
    @Schema(description = "分片上传，分片大小（单位：兆）")
    private Long partSize;

    /**
     * 是否启用断点上传（false：否，true：是）.
     */
    @Schema(description = "是否启用断点上传（false：否，true：是）")
    private Boolean broke;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 配置主键.
     */
    @Schema(description = "配置主键")
    private Long configId;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
