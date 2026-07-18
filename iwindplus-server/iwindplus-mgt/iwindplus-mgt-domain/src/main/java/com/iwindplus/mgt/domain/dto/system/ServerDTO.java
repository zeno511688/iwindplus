/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 服务数据传输对象.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "服务数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ServerDTO extends DbVersionBaseDTO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    @NotBlank(message = "{serverName.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{serverName.length}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 路由ID.
     */
    @Schema(description = "路由ID")
    @NotBlank(message = "{routeId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{routeId.length}", groups = {SaveGroup.class, EditGroup.class})
    private String routeId;

    /**
     * 服务地址.
     */
    @Schema(description = "服务地址")
    @NotBlank(message = "{uri.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{uri.length}", groups = {SaveGroup.class, EditGroup.class})
    private String uri;

    /**
     * 路由规则.
     */
    @Schema(description = "路由规则")
    @NotEmpty(message = "{predicates.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private List<ServerRouteParamDTO> predicates;

    /**
     * 路由过滤规则.
     */
    @Schema(description = "路由过滤规则")
    private List<ServerRouteParamDTO> filters;

    /**
     * 元数据.
     */
    @Schema(description = "元数据")
    @NotEmpty(message = "{metadata.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Map<String, Object> metadata;

    /**
     * 排序号.
     */
    @Schema(description = "排序号")
    private Integer seq;

    /**
     * 是否隐藏.
     */
    @Schema(description = "是否隐藏")
    private Boolean hideFlag;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;
}
