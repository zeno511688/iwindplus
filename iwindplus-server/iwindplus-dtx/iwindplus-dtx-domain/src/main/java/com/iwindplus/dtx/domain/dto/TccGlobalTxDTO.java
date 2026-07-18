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
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * tcc全局事务数据传输对象.
 *
 * @author zengdegui
 * @since 2026/02/04 20:57
 */
@Schema(description = "tcc全局事务数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TccGlobalTxDTO extends DbVersionBaseDTO {

    /**
     * 状态.
     */
    @Schema(description = "状态")
    private GlobalTxStatusEnum status;

    /**
     * 全局事务ID
     */
    @Schema(description = "全局事务ID")
    @NotBlank(message = "{xid.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{xid.length}", groups = {SaveGroup.class, EditGroup.class})
    private String xid;

    /**
     * 业务类型（订单 / 支付 等）.
     */
    @Schema(description = "业务类型（订单 / 支付 等）")
    @NotBlank(message = "{bizType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{bizType.length}", groups = {SaveGroup.class, EditGroup.class})
    private String bizType;

    /**
     * 环境.
     */
    @Schema(description = "环境")
    private String env;

    /**
     * 超时时间.
     */
    @Schema(description = "超时时间")
    private Long timeoutSeconds;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 下一次重试时间.
     */
    @Schema(description = "下一次重试时间")
    private LocalDateTime nextRetryTime;

    /**
     * 重试次数.
     */
    @Schema(description = "重试次数")
    private Integer retryCount;
}
