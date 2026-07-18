/**
 * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 */

package com.iwindplus.im.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public class ImConstant {

    private ImConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 即时通讯服务名.
     */
    public static final String IM_SERVER_NAME = "iwindplus-im";

    /**
     * 即时通讯服务客户端扫描包名.
     */
    public static final String IM_CLIENT_SCAN_BASE_PACKAGE = "com.iwindplus.im.client";

    /**
     * 聊天群主键.
     */
    public static final String CHAT_GROUP_ID = "chatGroupId";

    /**
     * 群组.
     */
    public static final String GROUP_LEADER = "群主";

    /**
     * 成员.
     */
    public static final String MEMBER = "成员";

}
