/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.iwindplus.base.domain.constant.CommonConstant.NetWorkConstant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 请求协议枚举定义.
 *
 * @author zengdegui
 * @since 2018/10/10
 */
@Getter
@RequiredArgsConstructor
public enum RequestProtocolEnum implements BaseEnum<String> {

    /**
     * http.
     */
    HTTP(NetWorkConstant.HTTP, NetWorkConstant.HTTP_PREFIX, "http"),

    /**
     * https.
     */
    HTTPS(NetWorkConstant.HTTPS, NetWorkConstant.HTTPS_PREFIX, "https"),

    /**
     * 负载均衡.
     */
    LB(NetWorkConstant.LB, NetWorkConstant.LB_PREFIX, "负载均衡"),

    /**
     * ws.
     */
    WS(NetWorkConstant.WS, NetWorkConstant.WS_PREFIX, "ws"),

    /**
     * wss.
     */
    WSS(NetWorkConstant.WSS, NetWorkConstant.WSS_PREFIX, "wss");

    /**
     * 值.
     */
    @EnumValue
    private final String value;

    /**
     * 前缀.
     */
    private final String prefix;

    /**
     * 描述.
     */
    private final String desc;
}
