/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.setup.domain.dto;

import com.iwindplus.base.domain.dto.DbVersionBaseDTO;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import com.iwindplus.base.domain.validation.EditGroup;
import com.iwindplus.base.domain.validation.SaveGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

/**
 * 微信小程序配置数据传输对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "微信小程序配置数据传输对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WechatConfigMaDTO extends DbVersionBaseDTO {

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
    @NotBlank(message = "{name.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 50, message = "{name.length}", groups = {SaveGroup.class, EditGroup.class})
    private String name;

    /**
     * 小程序appId.
     */
    @Schema(description = "小程序appId")
    @NotBlank(message = "{accessKey.notEmpty}", groups = {SaveGroup.class, EditGroup.class})
    @Length(max = 100, message = "{accessKey.length}", groups = {SaveGroup.class, EditGroup.class})
    private String accessKey;

    /**
     * 小程序密钥.
     */
    @Schema(description = "小程序密钥")
    @NotBlank(message = "{secretKey.notEmpty}", groups = {SaveGroup.class})
    @Length(max = 100, message = "{secretKey.length}", groups = {SaveGroup.class})
    private String secretKey;

    /**
     * 消息推送token.
     */
    @Schema(description = "消息推送token")
    @Length(max = 100, message = "{token.length}", groups = {SaveGroup.class, EditGroup.class})
    private String token;

    /**
     * 消息推送加密密钥.
     */
    @Schema(description = "消息推送加密密钥")
    @Length(max = 100, message = "{aesKey.length}", groups = {SaveGroup.class, EditGroup.class})
    private String aesKey;

    /**
     * 消息推送数据格式，XML或者JSON.
     */
    @Schema(description = "消息推送数据格式，XML或者JSON")
    @Length(max = 100, message = "{msgDataFormat.length}", groups = {SaveGroup.class, EditGroup.class})
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
    @Length(max = 255, message = "{qrcode.length}", groups = {SaveGroup.class, EditGroup.class})
    private String qrcode;

    /**
     * 回调成功地址（用户授权登录方式用）.
     */
    @Schema(description = "回调成功地址（外网，用户授权登录方式用）")
    private String notifySuccessUrl;

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    private Long orgId;
}