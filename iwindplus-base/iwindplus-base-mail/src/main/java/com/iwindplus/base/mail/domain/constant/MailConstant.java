/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.mail.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2018/12/27
 */
public final class MailConstant {

    private MailConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * 主配置编码.
     */
    public static final String MAIN_CODE = "mail-main";

    /**
     * 备用配置编码.
     */
    public static final String BACKUP_CODE = "mail-backup";

}
