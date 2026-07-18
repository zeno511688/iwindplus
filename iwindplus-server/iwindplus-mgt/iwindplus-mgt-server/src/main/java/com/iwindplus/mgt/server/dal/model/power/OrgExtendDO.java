/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.model.power;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.base.domain.annotation.TableFieldSafe;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 组织扩展表（处理大字段）.
 *
 * @author zengdegui
 * @since 2019/6/12
 */
@Schema(description = "组织扩展对象")
@TableName(value = "`org_extend`")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrgExtendDO extends DbBaseDO {

    /**
     * 简介.
     */
    @Schema(description = "简介")
    @TableFieldSafe
    private String intro;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}
