/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.system;

import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 客户端分页视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "客户端分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientPageVO extends DbVersionBaseVO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 客户端id.
     */
    @Schema(description = "客户端id")
    private String clientId;

    /**
     * 客户端名称.
     */
    @Schema(description = "客户端名称")
    private String clientName;

    /**
     * 客户端签发时间.
     */
    @Schema(description = "客户端签发时间")
    private LocalDateTime clientIdIssuedAt;

    /**
     * 客户端密钥过期时间.
     */
    @Schema(description = "客户端密钥过期时间")
    private LocalDateTime clientSecretExpiresAt;
}
