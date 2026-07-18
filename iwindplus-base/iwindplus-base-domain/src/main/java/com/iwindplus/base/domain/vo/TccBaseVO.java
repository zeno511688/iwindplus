/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * tcc基础信息视图对象.
 *
 * @author zengdegui
 * @since 2026/02/06 20:38
 */
@Schema(description = "tcc基础信息视图对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TccBaseVO implements Serializable {

    /**
     * 全局事务 ID
     */
    @Schema(description = "全局事务 ID")
    private String xid;
}
