/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.domain.dto;

import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * tcc全局事务搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "tcc全局事务搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TccGlobalTxSearchDTO extends DbPageDTO {

    /**
     * 状态.
     */
    @Schema(description = "状态")
    private GlobalTxStatusEnum status;

    /**
     * 状态列表.
     */
    @Schema(description = "状态列表")
    private List<GlobalTxStatusEnum> statusList;

    /**
     * 全局事务ID
     */
    @Schema(description = "全局事务ID")
    private String xid;

    /**
     * 业务类型（订单 / 支付 等）.
     */
    @Schema(description = "业务类型（订单 / 支付 等）")
    private String bizType;

    /**
     * 环境.
     */
    @Schema(description = "环境")
    private String env;

    /**
     * 过期时间.
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 重试时间.
     */
    @Schema(description = "重试时间")
    private LocalDateTime retryTime;

    /**
     * 重试次数.
     */
    @Schema(description = "重试次数")
    private Integer retryCount;
}
