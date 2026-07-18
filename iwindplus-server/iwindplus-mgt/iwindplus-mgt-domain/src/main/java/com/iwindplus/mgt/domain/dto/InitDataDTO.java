/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 初始化数据传输对象（将角色对应的数据初始化至新组织或用户）.
 *
 * @author zengdegui
 * @since 2024/08/16 23:00
 */
@Schema(description = "初始化数据传输对象")
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitDataDTO implements Serializable {

    /**
     * 角色主键集合.
     */
    @Schema(description = "角色主键集合")
    @NotEmpty(message = "{roleIds.notEmpty}")
    private Set<Long> roleIds;

    /**
     * 组织主键（新组织）.
     */
    @Schema(description = "组织主键")
    private Long orgId;

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;
}
