/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.log.domain.vo;

import com.iwindplus.base.domain.vo.UserBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 登录信息详情视图对象.
 *
 * @author zengdegui
 * @since 2019/2/15
 */
@Schema(description = "登录信息详情视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LoginInfoVO extends UserBaseVO {
    /**
     * 登录ip.
     */
    @Schema(description = "登录ip")
    private String loginIp;

    /**
     * 登录省份.
     */
    @Schema(description = "登录省份")
    private String loginProvince;

    /**
     * 登录城市.
     */
    @Schema(description = "登录城市")
    private String loginCity;

    /**
     * 登录时间.
     */
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}
