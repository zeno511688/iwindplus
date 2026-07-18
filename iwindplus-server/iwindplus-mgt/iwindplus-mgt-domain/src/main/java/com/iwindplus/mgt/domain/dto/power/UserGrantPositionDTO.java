/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 用户授权职位数据传输对象.
 *
 * @author zengdegui
 * @since 2021/5/5
 */
@Schema(description = "用户授权职位数据传输对象")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGrantPositionDTO implements Serializable {

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    @NotNull(message = "{userId.notEmpty}")
    private Long userId;

    /**
     * 职位主键集合.
     */
    @Schema(description = "职位主键集合")
    private Set<Long> positionIds;
}
