/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 组织保存数据传输对象.
 *
 * @author zengdegui
 * @since 2019/8/23
 */
@Schema(description = "组织保存数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgSaveDTO extends OrgDTO {

    /**
     * 简介.
     */
    @Schema(description = "简介")
    private String intro;
}
