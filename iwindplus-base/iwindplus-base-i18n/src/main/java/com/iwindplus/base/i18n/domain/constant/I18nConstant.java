/*
 *
 *  * Copyright (c) iwindplus Technologies Co., Ltd.2024-2030, All rights reserved.
 *
 *
 */

package com.iwindplus.base.i18n.domain.constant;

import com.iwindplus.base.domain.constant.CommonConstant;

/**
 * 常数.
 *
 * @author zengdegui
 * @since 2020/11/8
 */
public final class I18nConstant {

    private I18nConstant() {
        throw new IllegalStateException(CommonConstant.UTILITY_CLASS);
    }

    /**
     * Nacos i18n分组.
     */
    public static final String I18N_GROUP = "I18N_GROUP";

    /**
     * i18n文件后缀.
     */
    public static final String FILE_SUFFIX = ".properties";

}
