/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.annotation.EnumValid;
import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.mgt.domain.enums.ResourceTypeEnum;
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
 * 资源数据传输对象.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "资源数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO extends DbVersionBaseDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 类型（BUTTON：按钮，API：API）.
     */
    @Schema(description = "类型（BUTTON：按钮，API：API）")
    @NotNull(message = "{resourceType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @EnumValid(message = "{resourceType.illegal}", clazz = ResourceTypeEnum.class, groups = {SaveGroup.class, EditGroup.class})
    private ResourceTypeEnum resourceType;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    @Length(max = 50, message = "{code.length}", groups = {SaveGroup.class, EditGroup.class})
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    @NotBlank(message = "{name.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{name.length}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 请求方式.
     */
    @Schema(description = "请求方式")
    @NotBlank(message = "{requestMethod.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 10, message = "{requestMethod.length}", groups = {SaveGroup.class, EditGroup.class})
    private String requestMethod;

    /**
     * API路径.
     */
    @Schema(description = "API路径")
    @NotBlank(message = "{apiUrl.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{apiUrl.length}", groups = {SaveGroup.class, EditGroup.class})
    private String apiUrl;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 菜单主键.
     */
    @Schema(description = "菜单主键")
    @NotNull(message = "{menuId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long menuId;
}
