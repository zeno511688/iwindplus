/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.base.domain.annotation.TableFieldSafe;
import com.iwindplus.dtx.domain.enums.BranchTxStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * tcc分支事务表.
 *
 * @author zengdegui
 * @since 2026/02/04 20:57
 */
@Schema(description = "tcc分支事务对象")
@TableName(value = "`tcc_branch_tx`", autoResultMap = true)
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TccBranchTxDO extends DbBaseDO {

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

    /**
     * 上下文路径.
     */
    @Schema(description = "上下文路径")
    private String contextPath;

    /**
     * Confirm 回调地址.
     */
    @Schema(description = "Confirm 回调地址")
    private String confirmUrl;

    /**
     * Cancel 回调地址.
     */
    @Schema(description = "Cancel 回调地址")
    private String cancelUrl;

    /**
     * 参数.
     */
    @TableFieldSafe
    @Schema(description = "参数")
    private String payload;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    private String errorMsg;
}
