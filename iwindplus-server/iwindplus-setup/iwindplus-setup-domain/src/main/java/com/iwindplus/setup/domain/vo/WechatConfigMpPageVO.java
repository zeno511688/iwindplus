/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.vo;

import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 微信公众号配置分页视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "微信公众号配置分页视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WechatConfigMpPageVO extends DbVersionBaseVO {

    /**
     * 状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）.
     */
    @Schema(description = "状态（DISABLE：禁用，ENABLE：启用，LOCKED：锁定）")
    private EnableStatusEnum status;

    /**
     * 编码.
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称.
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 公众号appId.
     */
    @Schema(description = "公众号appId")
    private String accessKey;

    /**
     * 回调地址（外网，扫码登陆用）.
     */
    @Schema(description = "回调地址（外网，扫码登陆用）")
    private String notifyUrl;

    /**
     * 回调成功地址（扫码登陆用）.
     */
    @Schema(description = "回调成功地址（外网，扫码登陆用）")
    private String notifySuccessUrl;
}