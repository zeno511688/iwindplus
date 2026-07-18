/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.dtx.domain.enums.BranchTxStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * tcc分支事务数据传输对象.
 *
 * @author zengdegui
 * @since 2026/02/04 20:57
 */
@Schema(description = "tcc分支事务对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TccBranchTxDTO extends DbVersionBaseDTO {

    /**
     * 状态.
     */
    @Schema(description = "状态")
    private BranchTxStatusEnum status;

    /**
     * 全局事务 ID
     */
    @Schema(description = "全局事务ID")
    @NotBlank(message = "{xid.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{xid.length}", groups = {SaveGroup.class, EditGroup.class})
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
    @NotBlank(message = "{contextPath.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{contextPath.length}", groups = {SaveGroup.class, EditGroup.class})
    private String contextPath;

    /**
     * Confirm 回调地址.
     */
    @Schema(description = "Confirm 回调地址")
    @NotBlank(message = "{confirmUrl.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{confirmUrl.length}", groups = {SaveGroup.class, EditGroup.class})
    private String confirmUrl;

    /**
     * Cancel 回调地址.
     */
    @Schema(description = "Cancel 回调地址")
    @NotBlank(message = "{cancelUrl.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{cancelUrl.length}", groups = {SaveGroup.class, EditGroup.class})
    private String cancelUrl;

    /**
     * 参数.
     */
    @NotBlank(message = "{payload.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Schema(description = "参数")
    private String payload;

    /**
     * 错误信息.
     */
    @Schema(description = "错误信息")
    private String errorMsg;
}
