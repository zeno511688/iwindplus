/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.power;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import com.iwindplus.mgt.domain.enums.YubikeyBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户扩展yubikey配置数据传输对象.
 *
 * @author zengdegui
 * @since 2020/3/25
 */
@Schema(description = "用户扩展yubikey配置数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserExtendYubikeyDTO extends DbVersionBaseDTO {

    /**
     * 用户主键.
     */
    @Schema(description = "用户主键")
    @NotNull(message = "{userId.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private Long userId;

    /**
     * yubikey公钥.
     */
    @Schema(description = "yubikey公钥")
    @NotBlank(message = "{yubikeyPublicKey.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private String yubikeyPublicKey;

    /**
     * 业务类型.
     */
    @Schema(description = "业务类型")
    @NotNull(message = "{bizType.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    private YubikeyBizTypeEnum bizType;
}
