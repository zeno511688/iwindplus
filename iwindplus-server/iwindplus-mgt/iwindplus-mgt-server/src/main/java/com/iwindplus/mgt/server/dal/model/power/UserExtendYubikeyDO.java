/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.server.dal.model.power;

import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.mgt.domain.enums.YubikeyBizTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户扩展yubikey配置表.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "用户扩展yubikey配置对象")
@TableName(value = "`user_extend_yubikey`")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserExtendYubikeyDO extends DbBaseDO {

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