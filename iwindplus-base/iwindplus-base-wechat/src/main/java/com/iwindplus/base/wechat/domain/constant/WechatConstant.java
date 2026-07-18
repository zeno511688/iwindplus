/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.wechat.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 微信常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class WechatConstant {
    private WechatConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 微信开放平台redis存储前缀.
     */
    public static final String WECHAT_OPEN_PREFIX = "wechat_open";

    /**
     * 微信公众号redis存储前缀.
     */
    public static final String WECHAT_MP_PREFIX = "wechat_mp";

    /**
     * 微信小程序redis存储前缀.
     */
    public static final String WECHAT_MA_PREFIX = "wechat_ma";

    /**
     * 频率限制.
     */
    public static final int FREQUENCY_LIMIT = 45009;

    /**
     * page不合法（页面不存在或者小程序没有发布、根路径前加 /或者携带参数）.
     */
    public static final int PAGE_ILLEGAL = 41030;

    /**
     * 频率限制，每个用户每分钟100次（微信小程序）.
     */
    public static final int WECHAT_MA_FREQUENCY_LIMIT = 45011;

    /**
     * 无效code.
     */
    public static final int INVALID_CODE = 40029;

    /**
     * code只能使用一次.
     */
    public static final int CODE_CAN_USE_ONCE = 40163;

    /**
     * 高风险等级用户，小程序登录拦截（微信小程序）.
     */
    public static final int HIGH_RISK_USER = 40226;

    /**
     * 失败.
     */
    public static final int FAILED = -1;

    /**
     * code.
     */
    public static final String CODE = "code";

    /**
     * 绑定标记.
     */
    public static final String BIND_FLAG = "bindFlag";

    /**
     * 昵称.
     */
    public static final String NICK_NAME = "nickName";

    /**
     * 性别.
     */
    public static final String SEX = "sex";

    /**
     * 头像.
     */
    public static final String AVATAR = "avatar";

    /**
     * 国家.
     */
    public static final String COUNTRY = "country";

    /**
     * 省份.
     */
    public static final String PROVINCE = "province";

    /**
     * 城市.
     */
    public static final String CITY = "city";
}
