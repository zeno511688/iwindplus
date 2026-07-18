/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.vo.power;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户扩展视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "用户扩展视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserExtendVO extends UserVO {

    /**
     * 头像地址（绝对路径）.
     */
    @Schema(description = "头像地址（绝对路径）")
    private String avatarStr;

    /**
     * 身份证正面（绝对路径）.
     */
    @Schema(description = "身份证正面（绝对路径）")
    private String idCardFrontStr;

    /**
     * 身份证背面（绝对路径）.
     */
    @Schema(description = "身份证背面（绝对路径）")
    private String idCardBackStr;
}
