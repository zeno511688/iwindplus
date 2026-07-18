/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户组扩展视图对象.
 *
 * @author zengdegui
 * @since 2018/9/17
 */
@Schema(description = "用户组扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupExtendVO extends UserGroupVO {

    /**
     * 组织编码.
     */
    @Schema(description = "组织编码")
    private String orgCode;

    /**
     * 组织名称.
     */
    @Schema(description = "组织名称")
    private String orgName;

    /**
     * 组织简称.
     */
    @Schema(description = "组织简称")
    private String orgAbbr;
}
