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
 * 组织扩展视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "组织扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OrgExtendVO extends OrgVO {

    /**
     * 简介.
     */
    @Schema(description = "简介")
    private String intro;

    /**
     * 营业执照（用于显示）.
     */
    @Schema(description = "营业执照")
    private String businessLicenseStr;

    /**
     * logo地址（用于显示）.
     */
    @Schema(description = "logo地址")
    private String logoStr;
}
