/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.vo;

import com.iwindplus.base.domain.annotation.Sensitive;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.enums.SensitiveTypeEnum;
import com.iwindplus.base.domain.vo.DbVersionBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 微信小程序配置视图对象.
 *
 * @author zengdegui
 * @since 2021/9/23
 */
@Schema(description = "微信小程序配置视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WechatConfigMaVO extends DbVersionBaseVO {

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
     * 小程序appId.
     */
    @Schema(description = "小程序appId")
    private String accessKey;

    /**
     * 小程序密钥.
     */
    @Schema(description = "小程序密钥")
    @Sensitive(type = SensitiveTypeEnum.CUSTOM, startInclude = 2, endReserve = 2)
    private String secretKey;

    /**
     * 消息推送token.
     */
    @Schema(description = "消息推送token")
    private String token;

    /**
     * 消息推送加密密钥.
     */
    @Schema(description = "消息推送加密密钥")
    private String aesKey;

    /**
     * 消息推送数据格式，XML或者JSON.
     */
    @Schema(description = "消息推送数据格式，XML或者JSON")
    private String msgDataFormat;

    /**
     * 是否使用redis存储accessToken.
     */
    @Schema(description = "是否使用redis存储accessToken")
    private Boolean useRedis;

    /**
     * 小程序二维码.
     */
    @Schema(description = "小程序二维码")
    private String qrcode;

    /**
     * 回调成功地址（用户授权登录方式用）.
     */
    @Schema(description = "回调成功地址（外网，用户授权登录方式用）")
    private String notifySuccessUrl;

    /**
     * 小程序二维码地址（绝对路径）.
     */
    @Schema(description = "小程序二维码地址（绝对路径）")
    private String qrcodeStr;
}
