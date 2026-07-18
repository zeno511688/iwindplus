/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.model.system;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.mgt.domain.dto.system.ServerRouteParamDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 服务表.
 *
 * @author zengdegui
 * @since 2020/4/14
 */
@Schema(description = "服务对象")
@TableName(value = "`server`", autoResultMap = true)
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServerDO extends DbBaseDO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 路由ID.
     */
    @Schema(description = "路由ID")
    private String routeId;

    /**
     * 服务地址.
     */
    @Schema(description = "服务地址")
    private String uri;

    /**
     * 路由规则.
     */
    @Schema(description = "路由规则")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ServerRouteParamDTO> predicates;

    /**
     * 路由过滤规则.
     */
    @Schema(description = "路由过滤规则")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ServerRouteParamDTO> filters;

    /**
     * 元数据.
     */
    @Schema(description = "元数据")
    @TableField(typeHandler = JacksonTypeHandler.class)
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
