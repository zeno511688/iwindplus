/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.dtx.domain.enums.BranchTxStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * tcc分支事务搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "tcc分支事务搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TccBranchTxSearchDTO extends DbPageDTO {

    /**
     * 状态.
     */
    @Schema(description = "状态")
    private BranchTxStatusEnum status;

    /**
     * 全局事务 ID
     */
    @Schema(description = "全局事务ID")
    private String xid;

    /**
     * 分支事务ID.
     */
    @Schema(description = "分支事务ID")
    private Long branchId;
}
