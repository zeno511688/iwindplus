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
 * 微信公众号配置视图对象.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "微信公众号配置视图对象")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WechatConfigMpVO extends DbVersionBaseVO {

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
     * 公众号密钥.
     */
    @Schema(description = "公众号密钥")
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
     * 是否使用redis存储.
     */
    @Schema(description = "是否使用redis存储")
    private Boolean useRedis;

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