/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.OssTypeEnum;
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
 * 对象存储配置数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "对象存储配置数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OssConfigDTO extends DbVersionBaseDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    @NotBlank(message = "{name.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{name.length}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 类型.
     */
    @Schema(description = "类型")
    @NotNull(message = "{ossType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private OssTypeEnum type;

    /**
     * oss地域节点（必填）.
     */
    @Schema(description = "oss地域节点")
    @Length(max = 100, message = "{ossEndpoint.length}", groups = {SaveGroup.class, EditGroup.class})
    private String ossEndpoint;

    /**
     * 访问key.
     */
    @Schema(description = "访问key")
    @NotBlank(message = "{accessKey.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{accessKey.length}", groups = {SaveGroup.class, EditGroup.class})
    private String accessKey;

    /**
     * 密匙.
     */
    @Schema(description = "密匙")
    @NotBlank(message = "{secretKey.notEmpty}", groups = {SaveGroup.class})
    @Length(max = 100, message = "{secretKey.length}", groups = {SaveGroup.class})
    private String secretKey;

    /**
     * sts地域节点（可选）.
     */
    @Schema(description = "sts地域节点")
    @Length(max = 100, message = "{stsEndpoint.length}", groups = {SaveGroup.class, EditGroup.class})
    private String stsEndpoint;

    /**
     * RAM角色（可选）.
     */
    @Schema(description = "RAM角色")
    private String roleArn;

    /**
     * RAM权限策略（可选，如果policy为空，则用户将获得该角色下所有权限）.
     */
    @Schema(description = "RAM权限策略")
    private String policy;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}