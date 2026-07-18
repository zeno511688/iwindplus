/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.mgt.domain.enums.YubikeyBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户扩展yubikey配置分页视图对象.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "用户扩展yubikey配置分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserExtendYubikeyPageVO extends DbVersionBaseDTO {

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    private Long userId;

    /**
     * yubikey公钥.
     */
    @Schema(description = "yubikey公钥")
    private String yubikeyPublicKey;

    /**
     * 业务类型.
     */
    @Schema(description = "业务类型")
    private YubikeyBizTypeEnum bizType;
}
