/*
 * *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 应用凭证类型枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum AppCertTypeEnum implements BaseEnum<Integer> {

    /**
     * 网关 API 签名 - 黑名单模式）
     */
    API_GATEWAY_SIGN_BLACKLIST(0, "网关API签名（黑名单模式）"),

    /**
     * 网关 API 签名 - 白名单模式
     */
    API_GATEWAY_SIGN_WHITELIST(1, "网关API签名（白名单模式）"),

    /**
     * 服务间调用签名（内部 RPC / 微服务通信）
     */
    SERVICE_INTERNAL_SIGN(2, "服务间调用签名"),

    ;

    /**
     * 值.
     */
    @EnumValue
    private final Integer value;

    /**
     * 描述.
     */
    private final String desc;
}
