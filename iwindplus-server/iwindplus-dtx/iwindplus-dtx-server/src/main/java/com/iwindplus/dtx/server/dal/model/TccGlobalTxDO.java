/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.server.dal.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * tcc全局事务表.
 *
 * @author zengdegui
 * @since 2026/02/04 20:57
 */
@Schema(description = "tcc全局事务对象")
@TableName(value = "`tcc_global_tx`")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TccGlobalTxDO extends DbBaseDO {

    /**
     * 状态.
     */
    @Schema(description = "状态")
    private GlobalTxStatusEnum status;

    /**
     * 全局事务 ID
     */
    @Schema(description = "全局事务 ID")
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
