/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.model.power;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户部门关系表.
 *
 * @author zengdegui
 * @since 2026/01/15
 */
@Schema(description = "用户部门关系对象")
@TableName(value = "`user_department`")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDepartmentDO extends DbBaseDO {

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * 部门主键.
     */
    @Schema(description = "部门主键")
    private Long departmentId;

    /**
     * 是否主要部门（false：否，true：是）.
     */
    @Schema(description = "是否主要部门（false：否，true：是）")
    private Boolean primaryFlag;
}
