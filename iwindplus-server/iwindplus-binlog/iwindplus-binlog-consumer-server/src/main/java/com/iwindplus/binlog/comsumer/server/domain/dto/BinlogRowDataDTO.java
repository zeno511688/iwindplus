/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.binlog.comsumer.server.domain.dto;

import com.iwindplus.base.domain.constant.CommonConstant.DbConstant;
import com.iwindplus.base.domain.constant.CommonConstant.DbSignConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * binlog 行数据传输对象.
 *
 * @author zengdegui
 * @since 2025/11/21 22:24
 */
@Schema(description = "binlog 行数据传输对象")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BinlogRowDataDTO implements Serializable {

    /**
     * 操作.
     */
    @Schema(description = "操作")
    private String op;

    /**
     * 事务.
     */
    @Schema(description = "事务")
    private Object transaction;

    /**
     * 处理时间（毫秒）.
     */
    @Schema(description = "处理时间（毫秒）")
    private Long tsMs;

    /**
     * 处理时间（微秒）.
     */
    @Schema(description = "处理时间（微秒）")
    private Long tsUs;

    /**
     * 处理时间（纳秒）.
     */
    @Schema(description = "处理时间（纳秒）")
    private Long tsNs;

    /**
     * 元数据.
     */
    @Schema(description = "元数据")
    private SourceMetaDTO source;

    /**
     * 操作前数据.
     */
    @Schema(description = "操作前数据")
    private Map<String, Object> before;

    /**
     * 操作后数据.
     */
    @Schema(description = "操作后数据")
    private Map<String, Object> after;

    /**
     * 获取数据主键.
     *
     * @return Long
     */
    public Long getDataId() {
        return Optional.ofNullable(this.getAfterId())
            .orElse(this.getBeforeId());
    }

    /**
     * 获取操作前的主键.
     *
     * @return Long
     */
    public Long getBeforeId() {
        return Optional.ofNullable(before)
            .map(m -> m.get(DbConstant.ID))
            .filter(Objects::nonNull)
            .map(m -> Long.valueOf(m.toString().trim()))
            .orElse(null);
    }

    /**
     * 获取操作前的加签盐.
     *
     * @return Long
     */
    public Long getBeforeSalt() {
        return Optional.ofNullable(before)
            .map(m -> m.get(DbSignConstant.SALT))
            .filter(Objects::nonNull)
            .map(m -> Long.valueOf(m.toString().trim()))
            .orElse(null);
    }

    /**
     * 获取操作前的签名.
     *
     * @return String
     */
    public String getBeforeSign() {
        return Optional.ofNullable(before)
            .map(m -> m.get(DbSignConstant.SIGN))
            .map(String::valueOf)
            .orElse(null);
    }

    /**
     * 获取操作后的主键.
     *
     * @return String
     */
    public Long getAfterId() {
        return Optional.ofNullable(after)
            .map(m -> m.get(DbConstant.ID))
            .filter(Objects::nonNull)
            .map(m -> Long.valueOf(m.toString().trim()))
            .orElse(null);
    }

    /**
     * 获取操作后的加签盐.
     *
     * @return String
     */
    public Long getAfterSalt() {
        return Optional.ofNullable(after)
            .map(m -> m.get(DbSignConstant.SALT))
            .filter(Objects::nonNull)
            .map(m -> Long.valueOf(m.toString().trim()))
            .orElse(null);
    }

    /**
     * 获取操作后的签名.
     *
     * @return String
     */
    public String getAfterSign() {
        return Optional.ofNullable(after)
            .map(m -> m.get(DbSignConstant.SIGN))
            .map(String::valueOf)
            .orElse(null);
    }
}
