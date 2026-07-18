/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.domain.dto;

import com.iwindplus.base.domain.enums.DbActionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 发送信息数据传输对象.
 *
 * @author zengdegui
 * @since 2025/11/21 22:24
 */
@Schema(description = "发送信息数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SendMsgDTO implements Serializable {

    /**
     * 环境.
     */
    @Schema(description = "环境")
    private String env;

    /**
     * 处理时间（毫秒）.
     */
    @Schema(description = "处理时间（毫秒）")
    private Long tsMs;

    /**
     * 数据库名.
     */
    @Schema(description = "数据库名")
    private String db;

    /**
     * 表名.
     */
    @Schema(description = "表名")
    private String table;

    /**
     * 操作类型.
     */
    @Schema(description = "操作类型")
    private DbActionTypeEnum actionType;

    /**
     * 操作前主键.
     */
    private Long beforeId;

    /**
     * 操作前的加签盐.
     */
    public Long beforeSalt;

    /**
     * 操作后主键.
     */
    private Long afterId;

    /**
     * 操作后的加签盐.
     */
    public Long afterSalt;

    /**
     * 信息.
     */
    private String message;
}
