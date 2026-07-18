/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.mgt.domain.dto.system;

import com.iwindplus.base.domain.dto.DbPageDTO;
import com.iwindplus.mgt.domain.enums.BindTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 第三方绑定授权搜索数据传输对象.
 *
 * @author zengdegui
 * @since 2023/07/26 22:38
 */
@Schema(description = "第三方绑定授权搜索数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdBindGrantSearchDTO extends DbPageDTO {

    /**
     * 类型（MP：微信公众号，MA：微信小程序，CP：企业微信）.
     */
    @Schema(description = "类型（MP：微信公众号，MA：微信小程序，CP：企业微信）")
    private BindTypeEnum type;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 用户唯一标识.
     */
    @Schema(description = "用户唯一标识")
    private String openid;

    /**
     * 工号
     */
    @Schema(description = "工号")
    private String jobNumber;

    /**
     * 用户手机.
     */
    @Schema(description = "用户手机")
    private String mobile;
}
