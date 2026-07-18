/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.mgt.domain.enums.ResourceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 资源视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "资源视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceVO extends DbVersionBaseVO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 类型（BUTTON：按钮，API：API）.
     */
    @Schema(description = "类型（BUTTON：按钮，API：API）")
    private ResourceTypeEnum resourceType;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 请求方式.
     */
    @Schema(description = "请求方式")
    private String requestMethod;

    /**
     * API路径.
     */
    @Schema(description = "API路径")
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
    private Long menuId;
}