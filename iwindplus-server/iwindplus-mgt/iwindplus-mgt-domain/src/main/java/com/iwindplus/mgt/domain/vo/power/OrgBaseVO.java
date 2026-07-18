/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import com.iwindplus.base.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;







/**
 * 组织基础字段视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "组织基础字段视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgBaseVO extends BaseVO {

    /**
     * 简称.
     */
    @Schema(description = "简称")
    private String abbr;
}
