/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.dtx.domain.vo;

import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import com.iwindplus.dtx.domain.enums.GlobalTxStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * tcc全局事务分页视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "tcc全局事务分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TccGlobalTxPageVO extends DbVersionBaseVO {

    /**
     * 状态.
     */
    @Schema(description = "状态")
    private GlobalTxStatusEnum status;

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
