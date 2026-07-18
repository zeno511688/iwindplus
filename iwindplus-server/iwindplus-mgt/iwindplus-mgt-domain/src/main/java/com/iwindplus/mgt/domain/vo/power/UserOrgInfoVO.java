/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户组织信息视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户组织信息视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrgInfoVO implements Serializable {

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 用户姓名.
     */
    @Schema(description = "用户姓名")
    private String userRealName;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;

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
