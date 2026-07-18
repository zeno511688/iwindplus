/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.setup.server.dal.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iwindplus.base.mybatis.domain.DbBaseDO;
import com.iwindplus.base.domain.annotation.TableFieldSafe;
import com.iwindplus.base.domain.enums.EnableStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 微信公众号配置.
 *
 * @author zengdegui
 * @since 2018/9/1
 */
@Schema(description = "微信公众号配置对象")
@TableName(value = "wechat_config_mp")
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WechatConfigMpDO extends DbBaseDO {

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
    @TableFieldSafe
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

    /**
     * 是否内置（false：否，true：是）.
     */
    @Schema(description = "是否内置（false：否，true：是）")
    private Boolean buildInFlag;

    /**
     * 组织主键.
     */
    @Schema(description = "组织主键")
    @TableField(fill = FieldFill.INSERT)
    private Long orgId;
}